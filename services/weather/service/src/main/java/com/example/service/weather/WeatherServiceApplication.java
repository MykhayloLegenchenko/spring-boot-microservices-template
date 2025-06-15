package com.example.service.weather;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info =
        @Info(
            title = "Weather API",
            description = "Uses weatherapi.com to obtain weather information",
            version = "1.0",
            license =
                @License(
                    name = "Apache 2.0",
                    url = "https://www.apache.org/licenses/LICENSE-2.0.html")))
public class WeatherServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(WeatherServiceApplication.class, args);
  }
}
