package edu.uptc.enrollments.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enrollments Microservice API")
                        .description("Microservice responsible for managing student course enrollments, academic grades, and lifecycle audit history. " +
                                "Part of the UPTC Distributed Systems Modular University Architecture (Lab 3).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Marlon (Technical Lead)")
                                .email("marlon@uptc.edu.co"))
                        .license(new License()
                                .name("Educational Use Only - UPTC")
                                .url("https://www.uptc.edu.co")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Direct Local Service Instance"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway Routed Instance")
                ));
    }
}
