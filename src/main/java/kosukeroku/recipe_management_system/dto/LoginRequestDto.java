package kosukeroku.recipe_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

    @Data
    public class LoginRequestDto {

        @NotBlank(message = "Email cannot be blank!")
        private String email;

        @NotBlank(message = "Password cannot be blank!")
        private String password;
    }

