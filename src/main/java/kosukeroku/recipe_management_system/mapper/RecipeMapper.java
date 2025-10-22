package kosukeroku.recipe_management_system.mapper;

import kosukeroku.recipe_management_system.dto.RecipeRequestDto;
import kosukeroku.recipe_management_system.dto.RecipeResponseDto;
import kosukeroku.recipe_management_system.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;

@Component
public class RecipeMapper {

    public RecipeResponseDto toDto(Recipe recipe) {
        RecipeResponseDto recipeDto = new RecipeResponseDto();
        recipeDto.setName(recipe.getName());
        recipeDto.setDescription(recipe.getDescription());
        recipeDto.setIngredients(recipe.getIngredients());
        recipeDto.setDirections(recipe.getDirections());
        recipeDto.setCategory(recipe.getCategory());
        recipeDto.setDate(recipe.getDate().toString());

        if (recipe.getAuthor() != null) {
            recipeDto.setAuthorEmail(recipe.getAuthor().getEmail());
        }

        return recipeDto;
    }

    public Recipe toEntity(RecipeRequestDto recipeDto) {
        Recipe recipe = new Recipe();
        recipe.setName(recipeDto.getName());
        recipe.setDescription(recipeDto.getDescription());
        recipe.setIngredients(recipeDto.getIngredients());
        recipe.setDirections(recipeDto.getDirections());
        recipe.setCategory(recipeDto.getCategory());
        recipe.setDate(LocalDateTime.now());
        return recipe;
    }
}