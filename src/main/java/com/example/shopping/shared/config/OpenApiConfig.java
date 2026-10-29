package com.example.shopping.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI shoppingOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Shopping Service API").version("1.0.0"));
    }
}

