package com.example.service.user.role;

import static com.example.client.user.role.dto.RoleEvent.TOPIC;
import static com.example.client.user.role.dto.RoleEvent.Type.*;
import static com.example.test.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import com.example.client.user.role.RoleBlockingClient;
import com.example.client.user.role.dto.GetAllRolesRequest;
import com.example.client.user.role.dto.RoleDto;
import com.example.client.user.role.dto.RoleEvent;
import com.example.common.security.jwt.JwtTokenService;
import com.example.common.uuid.UuidType;
import com.example.common.uuid.UuidUtils;
import com.example.common.web.client.blocking.BlockingClientFactory;
import com.example.service.user.configuration.AbstractIntegrationTest;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.web.client.RestClient;

@EmbeddedKafka
@NullUnmarked
class RoleApiTests extends AbstractIntegrationTest {
  private static final List<RoleDto> systemRoles =
      Stream.of("USER", "REFRESH", "ADMIN", "SUPER").map(RoleDto::new).toList();

  private static RoleBlockingClient client;
  private static RoleBlockingClient adminClient;
  private static RoleBlockingClient noAuthClient;

  private RoleDto role;

  @BeforeAll
  static void setUpAll(@LocalServerPort int port, @Autowired JwtTokenService tokenService) {
    var factory =
        BlockingClientFactory.create(RestClient.builder().baseUrl("http://localhost:" + port));

    noAuthClient = factory.createClient(RoleBlockingClient.class);

    client =
        factory
            .duplicate()
            .bearerAuth(() -> tokenService.createToken(UuidUtils.randomUUID(UuidType.USER), "user"))
            .createClient(RoleBlockingClient.class);

    adminClient =
        factory
            .bearerAuth(
                () -> tokenService.createToken(UuidUtils.randomUUID(UuidType.USER), "admin"))
            .createClient(RoleBlockingClient.class);
  }

  @Test
  void testApiCalls() {
    testCreateRole();
    testUpdateRole();
    testGetAllRoles();
    testDeleteRole();
  }

  private void testCreateRole() {
    var request = new RoleDto("ROLE_1");

    assertSecured(api -> api.createRole(request));
    asserSystemRolesSecured(adminClient::createRole);

    var response = adminClient.createRole(request);
    assertThat(response).isEqualTo(request);
    assertRoleEvent(request.name(), CREATE, response);

    assertConflict(() -> adminClient.createRole(request));
    role = response;
  }

  private void testUpdateRole() {
    var request = new RoleDto("UPDATED_ROLE_1");

    asserSystemRolesSecured(r -> adminClient.updateRole(r.name(), request));
    assertSecured(api -> api.updateRole(role.name(), request));
    assertConflict(() -> adminClient.updateRole(role.name(), new RoleDto("TEST_ROLE_1")));

    var response = adminClient.updateRole(role.name(), request);
    assertThat(response).isEqualTo(request);
    assertRoleEvent(request.name(), UPDATE, response);

    assertNotFound(() -> adminClient.updateRole(role.name(), request));
    role = response;
  }

  private void testGetAllRoles() {
    var request = GetAllRolesRequest.builder().build();
    assertSecured(api -> api.getAllRoles(request));

    var response = adminClient.getAllRoles(request);
    assertThat(response).contains(role.name());
  }

  private void testDeleteRole() {
    asserSystemRolesSecured(r -> adminClient.deleteRole(r.name()));
    assertSecured(api -> api.deleteRole(role.name()));

    adminClient.deleteRole(role.name());
    assertRoleEvent(role.name(), DELETE, role);

    assertNotFound(() -> adminClient.deleteRole(role.name()));
  }

  private static void assertSecured(Consumer<RoleBlockingClient> caller) {
    assertUnauthorized(() -> caller.accept(noAuthClient));
    assertForbidden(() -> caller.accept(client));
  }

  private static void asserSystemRolesSecured(Consumer<RoleDto> caller) {
    systemRoles.forEach(r -> assertBadRequest(() -> caller.accept(r)));
  }

  private void assertRoleEvent(String name, RoleEvent.Type type, RoleDto role) {
    var event = testKafkaListener.getRoleEvent();
    assertThat(event.topic()).isEqualTo(TOPIC);
    assertThat(event.key()).isEqualTo(name);
    assertThat(event.value()).isEqualTo(new RoleEvent(type, role));
  }
}
