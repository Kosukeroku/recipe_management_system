package kosukeroku.recipe_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequestDto {
    @Email(message = "Invalid email!")
    @NotBlank(message = "Email cannot be blank!")
    String email;

    @NotBlank(message = "Password cannot be blank!")
    @Size(min = 8, message = "Password must have at least 8 characters.")
    String password;
}
