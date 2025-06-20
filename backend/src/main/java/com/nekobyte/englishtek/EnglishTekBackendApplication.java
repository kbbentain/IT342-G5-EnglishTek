package com.nekobyte.englishtek;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "EnglishTek LMS API",
        version = "1.0",
        description = "API documentation for EnglishTek Learning Management System",
        contact = @Contact(
            name = "EnglishTek Support",
            email = "support@englishtek.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    )
)
public class EnglishTekBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnglishTekBackendApplication.class, args);
    }
}
