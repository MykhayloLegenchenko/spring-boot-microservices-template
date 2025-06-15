package com.example.service.users;

import com.example.common.data.jpa.repository.support.ExtendedJpaRepositoryImpl;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@OpenAPIDefinition(
    info =
        @Info(
            title = "Users API",
            description = "Handles users, roles and authentication.",
            version = "1.0",
            license =
                @License(
                    name = "Apache 2.0",
                    url = "https://www.apache.org/licenses/LICENSE-2.0.html")))
@EnableJpaRepositories(repositoryBaseClass = ExtendedJpaRepositoryImpl.class)
public class UsersServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(UsersServiceApplication.class, args);
  }
}
