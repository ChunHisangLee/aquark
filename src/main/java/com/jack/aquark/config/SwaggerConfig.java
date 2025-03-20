package com.jack.aquark.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  private static final String API_DOC_TITLE = "Aquark REST API Documentation";

  @Bean
  public OpenAPI aquarkOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title(API_DOC_TITLE)
                .description("...")
                .version("v1.0.0")
                .contact(
                    new Contact()
                        .name("Jack Lee")
                        .email("jacklee@jack.com")
                        .url("https://www.jack.com"))
                .license(new License().name("Apache 2.0").url("https://www.jack.com")))
        .externalDocs(
            new ExternalDocumentation()
                .description(API_DOC_TITLE)
                .url("https://www.jack.com/swagger-ui.html"));
  }
}
