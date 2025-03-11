package com.jack.aquark;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Aquark REST API Documentation",
                description = "Aquark REST API Documentation",
                version = "v1.0.0",
                contact = @Contact(
                        name = "Jack Lee",
                        email = "jacklee@jack.com",
                        url = "https://www.jack.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.jack.com"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description =  "Aquark REST API Documentation",
                url = "https://www.jack.com/swagger-ui.html"
        )
)
public class AquarkApplication {

  public static void main(String[] args) {
    SpringApplication.run(AquarkApplication.class, args);
  }
}
