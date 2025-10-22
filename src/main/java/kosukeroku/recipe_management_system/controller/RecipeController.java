package kosukeroku.recipe_management_system.controller;

import jakarta.validation.Valid;
import kosukeroku.recipe_management_system.dto.RecipeRequestDto;
import kosukeroku.recipe_management_system.dto.RecipeResponseDto;
import kosukeroku.recipe_management_system.security.UserPrincipal;
import kosukeroku.recipe_management_system.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    RecipeService recipeService;


    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("/new")
    public Map<String, Long> addRecipe(@RequestBody @Valid RecipeRequestDto recipe,
                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {

        String email = userPrincipal.getUsername();
        long id = recipeService.saveRecipe(recipe, email);
        return Map.of("id", id);
    }

    @GetMapping("/all")
    public List<RecipeResponseDto> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> getRecipe(@PathVariable("id") long id) {
        return recipeService.getRecipeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable Long id,
                             @AuthenticationPrincipal UserPrincipal userPrincipal) {

        String email = userPrincipal.getUsername();
        recipeService.deleteRecipeById(id, email);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRecipe(@PathVariable Long id, @RequestBody @Valid RecipeRequestDto recipe,
                             @AuthenticationPrincipal UserPrincipal userPrincipal) {

        String email = userPrincipal.getUsername();
        recipeService.updateRecipeById(id, recipe, email);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RecipeResponseDto>> searchRecipeByCategoryOrName(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {

        if (category == null && name == null) {
            return ResponseEntity.badRequest().build();
        }

        if (category != null && name != null) {
            return ResponseEntity.badRequest().build();
        }

        if (category != null) {
            List<RecipeResponseDto> recipes = recipeService.searchByCategory(category);
            return ResponseEntity.ok(recipes);
        } else {
            List<RecipeResponseDto> recipes = recipeService.searchByName(name);
            return ResponseEntity.ok(recipes);
        }


    }


}