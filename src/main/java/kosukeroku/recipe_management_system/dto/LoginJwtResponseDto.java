package kosukeroku.recipe_management_system.dto;

import lombok.Data;

@Data
public class LoginJwtResponseDto {
    private String token;
    private String type = "Bearer";
    private String email;

    public LoginJwtResponseDto(String token, String email) {
        this.token = token;
        this.email = email;
    }
}
