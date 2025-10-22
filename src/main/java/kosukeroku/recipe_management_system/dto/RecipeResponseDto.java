package kosukeroku.recipe_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponseDto {


    private String name;
    private String category;
    private String date;
    private String description;
    private List<String> ingredients;
    private List<String> directions;
    private String authorEmail;
}