package org.etmetmy.bn_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi bnServerApi() {
        return GroupedOpenApi.builder()
                .group("bn-server")
                .packagesToScan("org.etmetmy.bn_server")
                .build();
    }

    @Bean
    public OpenAPI bnServerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("isThisTeamTwoTeam API")
                        .version("v1")
                        .description("Backend API documentation for Team Two")
                        .contact(new Contact()
                                .name("Team Two")
                                .email("teamtwo@example.com")));
    }
}
