package kosukeroku.recipe_management_system.service;

import kosukeroku.recipe_management_system.dto.RecipeRequestDto;
import kosukeroku.recipe_management_system.dto.RecipeResponseDto;
import kosukeroku.recipe_management_system.exception.AccessDeniedException;
import kosukeroku.recipe_management_system.exception.RecipeNotFoundException;
import kosukeroku.recipe_management_system.exception.UserNotFoundException;
import kosukeroku.recipe_management_system.mapper.RecipeMapper;
import kosukeroku.recipe_management_system.model.Recipe;
import kosukeroku.recipe_management_system.model.User;
import kosukeroku.recipe_management_system.repository.RecipeRepository;
import kosukeroku.recipe_management_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional

public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final UserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepository, RecipeMapper recipeMapper, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.userRepository = userRepository;
    }

    public long saveRecipe(RecipeRequestDto recipeDto, String userEmail) {

        User author = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));

        Recipe recipe = recipeMapper.toEntity(recipeDto);
        recipe.setAuthor(author);
        Recipe savedRecipe = recipeRepository.save(recipe);
        return savedRecipe.getId();
    }

    public List<RecipeResponseDto> getAllRecipes() {
        return recipeRepository.findAll()
                .stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<RecipeResponseDto> getRecipeById(long id) {
        return recipeRepository.findById(id).map(recipeMapper::toDto);
    }

    public void deleteRecipeById(long id, String userEmail) {


        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!recipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You can only delete your own recipes");
        }
        recipeRepository.deleteById(id);
    }

    public void updateRecipeById(long id, RecipeRequestDto recipeDto, String userEmail) {

        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!existingRecipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You can only update your own recipes");
        }

        existingRecipe.setName(recipeDto.getName());
        existingRecipe.setCategory(recipeDto.getCategory());
        existingRecipe.setDescription(recipeDto.getDescription());
        existingRecipe.setIngredients(recipeDto.getIngredients());
        existingRecipe.setDirections(recipeDto.getDirections());
        existingRecipe.setDate(LocalDateTime.now());


        recipeRepository.save(existingRecipe);
    }

    public List<RecipeResponseDto> searchByCategory(String category) {

        return recipeRepository.findByCategoryIgnoreCaseOrderByDateDesc(category)
                .stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<RecipeResponseDto> searchByName(String name) {

        return recipeRepository.findByNameContainingIgnoreCaseOrderByDateDesc(name)
                .stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }

}