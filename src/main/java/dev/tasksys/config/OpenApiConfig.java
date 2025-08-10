package dev.tasksys.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskSys API")
                        .description("Task Management System with CRUD operations")
                        .version("1.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("mitchell")
                                .email("mitchell7970@gmail.com")));
    }
}