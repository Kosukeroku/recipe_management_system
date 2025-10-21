package kosukeroku.recipe_management_system;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@RequiredArgsConstructor
@EnableWebSecurity
public class RecipeManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecipeManagementSystemApplication.class, args);
    }

}
