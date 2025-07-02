package com.example.service.user.user;

import static com.example.client.user.user.dto.UserEvent.Type.*;
import static com.example.service.user.user.UserRepository.Spec.byUuid;
import static com.example.service.user.user.UserRepository.Spec.withRoles;
import static com.example.test.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import com.example.client.user.auth.AuthBlockingClient;
import com.example.client.user.auth.dto.LoginRequest;
import com.example.client.user.user.UserBlockingClient;
import com.example.client.user.user.dto.*;
import com.example.common.security.jwt.JwtTokenService;
import com.example.common.uuid.UuidType;
import com.example.common.uuid.UuidUtils;
import com.example.common.web.client.blocking.BlockingClientFactory;
import com.example.service.user.configuration.AbstractIntegrationTest;
import com.example.service.user.role.RoleRepository;
import com.example.service.user.role.model.RoleEntity;
import com.example.service.user.user.model.UserEntity;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

@NullUnmarked
class UserApiTest extends AbstractIntegrationTest {
  private static final String PASSWORD = "password1";
  private static final UpdateUserRequest conflictUpdateUserRequest =
      new UpdateUserRequest("john@example.com", "Fist", "Last");

  private static JwtTokenService tokenService;
  private static BlockingClientFactory factory;
  private static UserBlockingClient noAuthClient;
  private static AuthBlockingClient authClient;
  private static UserBlockingClient client;
  private static UserBlockingClient adminClient;
  private static UserBlockingClient superClient;

  @Autowired private TestMapper mapper;
  @Autowired private UserMapper userMapper;
  @Autowired private UserRepository userRepository;

  private UserEntity lastUser;

  private UserBlockingClient selfClient;
  private UserBlockingClient selfAdminClient;

  @BeforeAll
  static void setUpAll(@LocalServerPort int port, @Autowired JwtTokenService jwtTokenService) {
    tokenService = jwtTokenService;

    factory =
        BlockingClientFactory.create(RestClient.builder().baseUrl("http://localhost:" + port));

    noAuthClient = factory.createClient(UserBlockingClient.class);
    authClient = factory.createClient(AuthBlockingClient.class);
    client = createClient(UuidUtils.randomUUID(UuidType.USER), "user");
    adminClient = createClient(UuidUtils.randomUUID(UuidType.USER), "admin");
    superClient = createClient(UuidUtils.randomUUID(UuidType.USER), "admin", "super");
  }

  @Test
  void testApiCalls() {
    testRegisterUser();
    testUpdateUser();
    testUpdateUserByUUID();
    testGetUser();
    testGetUserByUUID();
    testFindAndCountUsers();
    testDisableUser();
    testEnableUser();
    testUserRoles();
    testDeleteUser();
  }

  private void testRegisterUser() {
    var request = new RegisterUserRequest("test1@example.com", " Fist1 ", " Last1 ", PASSWORD);
    var response = noAuthClient.registerUser(request);

    Assertions.assertNotNull(request.lastName());
    assertThat(mapper.toRegisterUserRequest(response, request.password()))
        .isEqualTo(
            new RegisterUserRequest(
                request.email(),
                request.firstName().strip(),
                request.lastName().strip(),
                request.password()));

    assertThat(response.uuid()).isNotNull();
    assertThat(UuidUtils.typeOf(response.uuid())).isEqualTo(UuidType.USER);
    assertThat(response.createdAt()).isBefore(Instant.now());
    assertThat(response.updatedAt()).isAfterOrEqualTo(response.createdAt());

    var user = loadUser(response.uuid());
    assertThat(userMapper.toUserDto(user)).isEqualTo(response);
    assertUserEventProduced(response.uuid(), REGISTER, userMapper.toUserDtoEx(user));
    assertLoginSuccess(response.email(), request.password(), response.uuid());

    assertConflict(() -> noAuthClient.registerUser(request));

    selfClient = createClient(response.uuid(), "user");
    selfAdminClient = createClient(response.uuid(), "admin");
    lastUser = user;
  }

  private void testUpdateUser() {
    var request = new UpdateUserRequest("test2@example.com", " Fist2 ", " Last2 ");
    assertSecured(api -> api.updateUser(request));

    var response = selfClient.updateUser(request);
    assertUpdated(request, response);

    assertConflict(() -> selfClient.updateUser(conflictUpdateUserRequest));
  }

  private void testUpdateUserByUUID() {
    var request = new UpdateUserRequest("test3@example.com", " Fist3 ", " Last3 ");

    assertAdminSecured(api -> api.updateUser(lastUser.getUuid(), request));

    var response = adminClient.updateUser(lastUser.getUuid(), request);
    assertThat(response.enabled()).isTrue();
    assertThat(response.deletedAt()).isNull();
    assertUpdated(request, response);

    assertConflict(() -> adminClient.updateUser(lastUser.getUuid(), conflictUpdateUserRequest));
  }

  private void testGetUser() {
    assertSecured(UserBlockingClient::getUser);
    assertThat(selfClient.getUser()).isEqualTo(userMapper.toUserDto(lastUser));
  }

  private void testGetUserByUUID() {
    assertAdminSecured(api -> api.getUser(lastUser.getUuid()));
    assertThat(adminClient.getUser(lastUser.getUuid())).isEqualTo(userMapper.toUserDtoEx(lastUser));
  }

  private void testFindAndCountUsers() {
    var findRequest = FindUsersRequest.builder().build();
    assertAdminSecured(api -> api.findUsers(findRequest));

    var findResult = adminClient.findUsers(findRequest);
    assertThat(findResult).contains(userMapper.toUserDtoEx(lastUser));

    var countRequest = CountUsersRequest.builder().build();
    assertAdminSecured(api -> api.countUsers(countRequest));

    var countResult = adminClient.countUsers(countRequest);
    assertThat(countResult.count()).isEqualTo(findResult.size());
  }

  private void testDisableUser() {
    assertAdminSecured(api -> api.disableUser(lastUser.getUuid()));
    assertBadRequest(() -> selfAdminClient.disableUser(lastUser.getUuid()));

    adminClient.disableUser(lastUser.getUuid());
    assertEnabledUpdated(false);
    assertLoginUnauthorised(lastUser.getEmail());
  }

  private void testEnableUser() {
    assertAdminSecured(api -> api.enableUser(lastUser.getUuid()));
    assertBadRequest(() -> selfAdminClient.enableUser(lastUser.getUuid()));

    adminClient.enableUser(lastUser.getUuid());
    assertEnabledUpdated(true);
    assertLoginSuccess(lastUser.getEmail(), PASSWORD, lastUser.getUuid());
  }

  private void testUserRoles() {
    var request = Set.of("TEST_ROLE_1", "TEST_ROLE_2");
    assertAdminSecured(api -> api.setRoles(lastUser.getUuid(), request));
    assertSecured(UserBlockingClient::getRoles);
    assertAdminSecured(api -> api.getRoles(lastUser.getUuid()));

    adminClient.setRoles(lastUser.getUuid(), request);
    assertRolesUpdated(request);

    assertThat(selfClient.getRoles()).isEqualTo(request);
    assertThat(adminClient.getRoles(lastUser.getUuid())).isEqualTo(request);

    var request2 = Set.of("TEST_ROLE_2", "TEST_ROLE_3");
    adminClient.setRoles(lastUser.getUuid(), request2);
    assertRolesUpdated(request2);
    assertThat(selfClient.getRoles()).isEqualTo(request2);
    assertThat(adminClient.getRoles(lastUser.getUuid())).isEqualTo(request2);

    var badRoleNames = new ArrayList<>(List.of("", "WHITE SPACE", "lowercase"));
    badRoleNames.addAll(RoleRepository.RESERVED_NAMES);

    for (var name : badRoleNames) {
      var reservedRequest = Set.of(name);
      assertBadRequest(() -> adminClient.setRoles(lastUser.getUuid(), reservedRequest));
      assertBadRequest(() -> superClient.setRoles(lastUser.getUuid(), reservedRequest));
    }

    for (var name : RoleRepository.PROTECTED_NAMES) {
      var protectedRequest = Set.of(name);
      assertForbidden(() -> adminClient.setRoles(lastUser.getUuid(), protectedRequest));
      superClient.setRoles(lastUser.getUuid(), protectedRequest);
      assertRolesUpdated(protectedRequest);
      assertThat(superClient.getRoles(lastUser.getUuid())).isEqualTo(protectedRequest);
    }
  }

  private void testDeleteUser() {
    assertAdminSecured(api -> api.enableUser(lastUser.getUuid()));
    assertBadRequest(() -> selfAdminClient.deleteUser(lastUser.getUuid()));

    adminClient.deleteUser(lastUser.getUuid());

    var user = loadUser(lastUser.getUuid());
    assertThat(user.getUpdatedAt()).isAfter(lastUser.getUpdatedAt());
    assertThat(user.getDeletedAt()).isAfter(lastUser.getUpdatedAt());
    assertUserEventProduced(lastUser.getUuid(), DELETE, userMapper.toUserDtoEx(user));

    // Test API on deleted user
    assertLoginUnauthorised(lastUser.getEmail());
    assertNotFound(
        () ->
            selfClient.updateUser(
                new UpdateUserRequest("test2@example.com", " Fist2 ", " Last2 ")));
    assertNotFound(selfClient::getUser);
    assertNotFound(selfClient::getRoles);
    assertThat(adminClient.getUser(lastUser.getUuid())).isEqualTo(userMapper.toUserDtoEx(user));
  }

  private static UserBlockingClient createClient(UUID userUuid, String... roles) {
    Assert.notEmpty(roles, "Roles must not be empty");
    return factory
        .duplicate()
        .bearerAuth(() -> tokenService.createToken(userUuid, roles))
        .createClient(UserBlockingClient.class);
  }

  private static void assertLoginSuccess(String email, String password, UUID uuid) {
    var authResponse = authClient.login(new LoginRequest(email, password));
    assertThat(authResponse.uuid()).isEqualTo(uuid);
  }

  private static void assertLoginUnauthorised(String email) {
    assertUnauthorized(() -> authClient.login(new LoginRequest(email, PASSWORD)));
  }

  void assertUserEventProduced(UUID uuid, UserEvent.Type type, UserDtoEx user) {
    var event = testKafkaListener.getUserEvent();
    assertThat(event).isNotNull();
    assertThat(event.key()).isEqualTo(uuid.toString());
    assertThat(event.value()).isEqualTo(new UserEvent(type, user));
  }

  UserEntity loadUser(UUID uuid) {
    return userRepository.fetchOne(withRoles(byUuid(uuid))).orElseThrow();
  }

  private static void assertSecured(Consumer<UserBlockingClient> caller) {
    assertUnauthorized(() -> caller.accept(noAuthClient));
  }

  private static void assertAdminSecured(Consumer<UserBlockingClient> caller) {
    assertSecured(caller);
    assertForbidden(() -> caller.accept(client));
  }

  private void assertUpdated(UpdateUserRequest request, UserData response) {
    Assertions.assertNotNull(request.lastName());
    assertThat(mapper.toUpdateUserRequest(response))
        .isEqualTo(
            new UpdateUserRequest(
                request.email(), request.firstName().strip(), request.lastName().strip()));
    assertThat(response.uuid()).isEqualTo(lastUser.getUuid());
    assertThat(response.createdAt()).isEqualTo(lastUser.getCreatedAt());
    assertThat(response.updatedAt()).isAfter(lastUser.getUpdatedAt());

    var user = loadUser(response.uuid());
    assertThat(user.getUpdatedAt()).isAfter(lastUser.getUpdatedAt());

    if (response instanceof UserDtoEx) {
      assertThat(userMapper.toUserDtoEx(user)).isEqualTo(response);
    } else {
      assertThat(userMapper.toUserDto(user)).isEqualTo(response);
    }

    assertUserEventProduced(response.uuid(), UPDATE, userMapper.toUserDtoEx(user));
    lastUser = user;
  }

  private void assertEnabledUpdated(boolean enabled) {
    var user = loadUser(lastUser.getUuid());

    assertThat(user.getUpdatedAt()).isAfter(lastUser.getUpdatedAt());
    assertThat(userMapper.toUserDtoEx(user))
        .isEqualTo(mapper.toUserDtoEx(lastUser, user.getUpdatedAt(), enabled));
    assertUserEventProduced(
        lastUser.getUuid(), enabled ? ENABLE : DISABLE, userMapper.toUserDtoEx(user));
    lastUser = user;
  }

  private void assertRolesUpdated(Set<String> roles) {
    lastUser = loadUser(lastUser.getUuid());
    assertThat(lastUser.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet()))
        .isEqualTo(roles);

    var event = testKafkaListener.getUserEvent();
    assertThat(event).isNotNull();
    assertThat(event.key()).isEqualTo(lastUser.getUuid().toString());
    assertThat(event.value())
        .isEqualTo(new UserEvent(SET_ROLES, userMapper.toUserDtoEx(lastUser), roles));
  }

  @Mapper
  interface TestMapper {
    @Mapping(target = "password", source = "password")
    RegisterUserRequest toRegisterUserRequest(UserDto src, String password);

    UpdateUserRequest toUpdateUserRequest(UserDto src);

    UpdateUserRequest toUpdateUserRequest(UserDtoEx src);

    default UpdateUserRequest toUpdateUserRequest(UserData src) {
      return src instanceof UserDtoEx userDtoEx
          ? toUpdateUserRequest(userDtoEx)
          : toUpdateUserRequest((UserDto) src);
    }

    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "enabled", source = "enabled")
    UserDtoEx toUserDtoEx(UserEntity src, Instant updatedAt, boolean enabled);
  }
}
