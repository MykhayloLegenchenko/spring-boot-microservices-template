package com.example.service.weather;

import static org.assertj.core.api.Assertions.*;

import com.example.client.weather.WeatherBlockingClient;
import com.example.client.weather.dto.CurrentWeatherResult;
import com.example.client.weather.dto.LocationDto;
import com.example.client.weather.dto.WeatherDto;
import com.example.client.weather.dto.WeatherRequest;
import com.example.common.security.jwt.JwtTokenService;
import com.example.common.uuid.UuidType;
import com.example.common.uuid.UuidUtils;
import com.example.common.web.client.blocking.BlockingClientFactory;
import com.example.test.web.client.reactive.MockWebClientService;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = WeatherServiceApplication.class)
@Import({JwtTokenService.class, MockWebClientService.class})
class WeatherApiTests {
  private static WeatherBlockingClient weatherClient;
  private static WeatherBlockingClient weatherNoAuthClient;

  @BeforeAll
  static void init(
      @LocalServerPort int port,
      @Autowired JwtTokenService tokenService,
      @Autowired MockWebClientService mockService)
      throws IOException, BindException {

    var factory =
        BlockingClientFactory.create(RestClient.builder().baseUrl("http://localhost:" + port));

    weatherNoAuthClient = factory.createClient(WeatherBlockingClient.class);
    weatherClient =
        factory
            .bearerAuth(() -> tokenService.createToken(UuidUtils.randomUUID(UuidType.USER), "user"))
            .createClient(WeatherBlockingClient.class);

    mockService.loadFromResources("responses");
  }

  @Test
  void currentWeather() {
    var request = new WeatherRequest("Odesa, Ukraine");

    assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class)
        .isThrownBy(() -> weatherNoAuthClient.current(request));

    var result = weatherClient.current(request);
    var expected =
        new CurrentWeatherResult(
            new LocationDto("Odesa", "Odes'ka Oblast'", "Ukraine", 46.47, 30.73, "Europe/Kiev"),
            new WeatherDto(25.6, 15.2, "S"));

    assertThat(result).isEqualTo(expected);
  }
}
