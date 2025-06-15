package com.example.service.users.user;

import static com.example.test.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import com.example.client.users.auth.AuthBlockingClient;
import com.example.client.users.auth.dto.LoginRequest;
import com.example.client.users.user.UserBlockingClient;
import com.example.client.users.user.dto.CountUsersRequest;
import com.example.client.users.user.dto.FindUsersRequest;
import com.example.client.users.user.dto.RegisterUserRequest;
import com.example.client.users.user.dto.UpdateUserRequest;
import com.example.client.users.user.dto.UserDto;
import com.example.client.users.user.dto.UserDtoEx;
import com.example.common.security.jwt.JwtTokenService;
import com.example.common.uuid.UuidType;
import com.example.common.uuid.UuidUtils;
import com.example.common.web.client.blocking.BlockingClientFactory;
import com.example.service.users.UsersServiceApplication;
import com.example.service.users.role.RoleRepository;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = UsersServiceApplication.class)
@Import(JwtTokenService.class)
@NullUnmarked
class UserApiTest {
  private static final UpdateUserRequest conflictUpdateUserRequest =
      new UpdateUserRequest("john@example.com", "Fist", "Last");

  private static JwtTokenService tokenService;
  private static BlockingClientFactory factory;
  private static UserBlockingClient client;
  private static UserBlockingClient adminClient;
  private static UserBlockingClient superClient;
  private static UserBlockingClient noAuthClient;

  @Autowired private TestMapper mapper;

  private UserDto user;
  private UserDtoEx userEx;

  private UserBlockingClient selfClient;
  private UserBlockingClient selfAdminClient;

  @BeforeAll
  static void init(@LocalServerPort int port, @Autowired JwtTokenService jwtTokenService) {
    tokenService = jwtTokenService;

    factory =
        BlockingClientFactory.create(RestClient.builder().baseUrl("http://localhost:" + port));

    noAuthClient = factory.createClient(UserBlockingClient.class);
    client = createClient(UuidUtils.randomUUID(UuidType.USER), "user");
    adminClient = createClient(UuidUtils.randomUUID(UuidType.USER), "admin");
    superClient = createClient(UuidUtils.randomUUID(UuidType.USER), "admin", "super");
  }

  @Test
  void apiCalls() {
    testRegisterUser();
    testUpdateUser();
    testUpdateUserByUUID();
    testGetUser();
    testGetUserByUUID();
    testFindAndCountUsers();
    testDisableUser();
    testEnableUser();
    testGetAndSetUserRoles();
    testDeleteUser();
  }

  private void testRegisterUser() {
    var request = new RegisterUserRequest("test1@example.com", " Fist1 ", " Last1 ", "password1");
    var response = noAuthClient.registerUser(request);
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

    assertConflict(() -> noAuthClient.registerUser(request));
    var authResponse =
        factory
            .createClient(AuthBlockingClient.class)
            .login(new LoginRequest(request.email(), request.password()));

    assertThat(authResponse.uuid()).isEqualTo(response.uuid());

    selfClient = createClient(response.uuid(), "user");
    selfAdminClient = createClient(response.uuid(), "admin");
    user = response;
  }

  private void testUpdateUser() {
    var request = new UpdateUserRequest("test2@example.com", " Fist2 ", " Last2 ");
    assertSecured(api -> api.updateUser(request));

    var response = selfClient.updateUser(request);
    assertUpdateResponse(request, response);

    assertConflict(() -> selfClient.updateUser(conflictUpdateUserRequest));

    user = response;
  }

  private void testUpdateUserByUUID() {
    var request = new UpdateUserRequest("test3@example.com", " Fist3 ", " Last3 ");

    assertAdminSecured(api -> api.updateUser(user.uuid(), request));

    var response = adminClient.updateUser(user.uuid(), request);
    assertUpdateResponse(request, mapper.toUserDto(response));
    assertThat(response.enabled()).isTrue();
    assertThat(response.deletedAt()).isNull();

    assertConflict(() -> adminClient.updateUser(user.uuid(), conflictUpdateUserRequest));

    user = mapper.toUserDto(response);
    userEx = response;
  }

  private void testGetUser() {
    assertSecured(UserBlockingClient::getUser);

    var result = selfClient.getUser();
    assertThat(result).isEqualTo(user);
  }

  private void testGetUserByUUID() {
    assertAdminSecured(api -> api.getUser(user.uuid()));

    var resul = adminClient.getUser(user.uuid());
    assertThat(resul).isEqualTo(userEx);
  }

  private void testFindAndCountUsers() {
    var findRequest = FindUsersRequest.builder().build();
    assertAdminSecured(api -> api.findUsers(findRequest));

    var findResult = adminClient.findUsers(findRequest);
    assertThat(findResult).contains(userEx);

    var countRequest = CountUsersRequest.builder().build();
    assertAdminSecured(api -> api.countUsers(countRequest));

    var countResult = adminClient.countUsers(countRequest);
    assertThat(countResult.count()).isEqualTo(findResult.size());
  }

  private void testDisableUser() {
    assertAdminSecured(api -> api.disableUser(user.uuid()));
    assertBadRequest(() -> selfAdminClient.disableUser(user.uuid()));

    adminClient.disableUser(user.uuid());
    assertThat(adminClient.getUser(user.uuid()).enabled()).isFalse();
  }

  private void testEnableUser() {
    assertAdminSecured(api -> api.enableUser(user.uuid()));
    assertBadRequest(() -> selfAdminClient.enableUser(user.uuid()));

    adminClient.enableUser(user.uuid());
    assertThat(adminClient.getUser(user.uuid()).enabled()).isTrue();
  }

  private void testDeleteUser() {
    assertAdminSecured(api -> api.enableUser(user.uuid()));
    assertBadRequest(() -> selfAdminClient.deleteUser(user.uuid()));
    adminClient.deleteUser(user.uuid());

    var deletedUser = adminClient.getUser(user.uuid());
    assertThat(deletedUser.deletedAt()).isAfter(deletedUser.createdAt());

    // Test API on deleted user
    assertNotFound(
        () ->
            selfClient.updateUser(
                new UpdateUserRequest("test2@example.com", " Fist2 ", " Last2 ")));
    assertNotFound(selfClient::getUser);
    assertNotFound(selfClient::getRoles);
  }

  private void testGetAndSetUserRoles() {
    var request = Set.of("TEST_ROLE_1", "TEST_ROLE_2");
    assertAdminSecured(api -> api.setRoles(user.uuid(), request));
    assertSecured(UserBlockingClient::getRoles);
    assertAdminSecured(api -> api.getRoles(user.uuid()));

    adminClient.setRoles(user.uuid(), request);

    var result = selfClient.getRoles();
    assertThat(result).isEqualTo(request);

    result = adminClient.getRoles(user.uuid());
    assertThat(result).isEqualTo(request);

    var request2 = Set.of("TEST_ROLE_2", "TEST_ROLE_3");
    adminClient.setRoles(user.uuid(), request2);
    result = selfClient.getRoles();
    assertThat(result).isEqualTo(request2);

    result = adminClient.getRoles(user.uuid());
    assertThat(result).isEqualTo(request2);

    for (var name : RoleRepository.PROTECTED_NAMES) {
      var protectedReg = Set.of(name);
      assertForbidden(() -> adminClient.setRoles(user.uuid(), protectedReg));
      superClient.setRoles(user.uuid(), protectedReg);
      result = superClient.getRoles(user.uuid());
      assertThat(result).isEqualTo(protectedReg);
    }
  }

  private void assertUpdateResponse(UpdateUserRequest request, UserDto response) {
    assertThat(mapper.toUpdateUserRequest(response))
        .isEqualTo(
            new UpdateUserRequest(
                request.email(), request.firstName().strip(), request.lastName().strip()));
    assertThat(response.uuid()).isEqualTo(user.uuid());
    assertThat(response.createdAt()).isEqualTo(user.createdAt());
  }

  private static UserBlockingClient createClient(UUID userUuid, String... roles) {
    Assert.notEmpty(roles, "Roles must not be empty");
    return factory
        .duplicate()
        .bearerAuth(() -> tokenService.createToken(userUuid, roles))
        .createClient(UserBlockingClient.class);
  }

  private static void assertSecured(Consumer<UserBlockingClient> caller) {
    assertUnauthorized(() -> caller.accept(noAuthClient));
  }

  private static void assertAdminSecured(Consumer<UserBlockingClient> caller) {
    assertSecured(caller);
    assertForbidden(() -> caller.accept(client));
  }

  @Mapper
  interface TestMapper {
    @Mapping(target = "password", source = "password")
    RegisterUserRequest toRegisterUserRequest(UserDto src, String password);

    UpdateUserRequest toUpdateUserRequest(UserDto src);

    UserDto toUserDto(UserDtoEx src);
  }
}
