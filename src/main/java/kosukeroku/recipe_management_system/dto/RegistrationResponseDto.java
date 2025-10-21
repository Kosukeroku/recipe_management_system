package kosukeroku.recipe_management_system.dto;

import kosukeroku.recipe_management_system.model.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationResponseDto {
    private Long id;
    private String email;
    private LocalDateTime createdAt;

    public RegistrationResponseDto (User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }
}