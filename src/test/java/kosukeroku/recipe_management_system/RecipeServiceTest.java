package kosukeroku.recipe_management_system;

import kosukeroku.recipe_management_system.dto.RecipeRequestDto;
import kosukeroku.recipe_management_system.dto.RecipeResponseDto;
import kosukeroku.recipe_management_system.exception.RecipeNotFoundException;
import kosukeroku.recipe_management_system.mapper.RecipeMapper;
import kosukeroku.recipe_management_system.model.Recipe;
import kosukeroku.recipe_management_system.repository.RecipeRepository;
import kosukeroku.recipe_management_system.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
public class RecipeServiceTest {

    @MockitoBean
    private RecipeRepository recipeRepository;

    @MockitoBean
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeService recipeService;


    private RecipeRequestDto requestDto;
    private Recipe entity;
    private Recipe anotherEntity;
    private Recipe savedEntity;
    private RecipeResponseDto responseDto;
    private RecipeResponseDto anotherResponseDto;

    private static final Long EXISTING_RECIPE_ID = 1L;
    private static final Long NON_EXISTING_RECIPE_ID = 999L;
    private static final Long ANOTHER_RECIPE_ID = 2L;
    private static final String LUNCH_CATEGORY = "lunch";
    private static final String DINNER_CATEGORY = "dinner";
    private static final String PASTA_SUBSTRING = "pasta";
    private static final String CAKE_SUBSTRING = "cake";

    @BeforeEach
    void setUp() {
        requestDto = new RecipeRequestDto(
                "Pasta Carbonara",
                "dinner",
                "Creamy Italian pasta with bacon",
                List.of("spaghetti", "eggs", "bacon", "parmesan"),
                List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all")
        );

        entity = new Recipe();
        entity.setName("Pasta Carbonara");
        entity.setCategory(DINNER_CATEGORY);
        entity.setDescription("Creamy Italian pasta with bacon");
        entity.setIngredients(List.of("spaghetti", "eggs", "bacon", "parmesan"));
        entity.setDirections(List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all"));
        entity.setDate(LocalDateTime.now());

        anotherEntity = new Recipe();
        anotherEntity.setId(ANOTHER_RECIPE_ID);
        anotherEntity.setName("Greek Salad");
        anotherEntity.setCategory(DINNER_CATEGORY);
        anotherEntity.setDescription("Fresh Mediterranean salad with feta cheese and olives");
        anotherEntity.setIngredients(List.of("tomatoes", "cucumber", "red onion", "feta cheese", "olives", "olive oil"));
        anotherEntity.setDirections(List.of("Chop all vegetables", "Cube feta cheese", "Mix everything in a bowl", "Dress with olive oil and herbs"));
        anotherEntity.setDate(LocalDateTime.now().minusHours(2));

        savedEntity = new Recipe();
        savedEntity.setId(EXISTING_RECIPE_ID);
        savedEntity.setName("Pasta Carbonara");
        savedEntity.setCategory("dinner");
        savedEntity.setDescription("Creamy Italian pasta with bacon");
        savedEntity.setIngredients(List.of("spaghetti", "eggs", "bacon", "parmesan"));
        savedEntity.setDirections(List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all"));
        savedEntity.setDate(LocalDateTime.now());

        responseDto = new RecipeResponseDto(
                "Pasta Carbonara",
                "dinner",
                LocalDateTime.now().toString(),
                "Creamy Italian pasta with bacon",
                List.of("spaghetti", "eggs", "bacon", "parmesan"),
                List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all")
        );

        anotherResponseDto = new RecipeResponseDto();
        anotherResponseDto.setName("Greek Salad");
        anotherResponseDto.setCategory(DINNER_CATEGORY);
        anotherResponseDto.setDate(LocalDateTime.now().minusHours(2).toString());
        anotherResponseDto.setDescription("Fresh Mediterranean salad with feta cheese and olives");
        anotherResponseDto.setIngredients(List.of("tomatoes", "cucumber", "red onion", "feta cheese", "olives", "olive oil"));
        anotherResponseDto.setDirections(List.of("Chop all vegetables", "Cube feta cheese", "Mix everything in a bowl", "Dress with olive oil and herbs"));
    }


    @Test
    public void saveRecipe_shouldReturnId() {

        when(recipeMapper.toEntity(requestDto)).thenReturn(entity);
        when(recipeRepository.save(entity)).thenReturn(savedEntity);

        Long id = recipeService.saveRecipe(requestDto);
        assertThat(id).isEqualTo(EXISTING_RECIPE_ID);
        verify(recipeRepository, times(1)).save(entity);
        verify(recipeMapper, times(1)).toEntity(requestDto);

    }

    @Test
    public void getRecipeByValidId_shouldReturnRecipe() {

        when(recipeMapper.toDto(entity)).thenReturn(responseDto);
        when(recipeRepository.findById(EXISTING_RECIPE_ID)).thenReturn(Optional.of(entity));

        Optional<RecipeResponseDto> result = recipeService.getRecipeById(EXISTING_RECIPE_ID);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(responseDto);
    }

    @Test
    public void getRecipeByInvalidId_shouldReturnEmptyOptional() {
        when(recipeMapper.toDto(entity)).thenReturn(responseDto);
        when(recipeRepository.findById(NON_EXISTING_RECIPE_ID)).thenReturn(Optional.empty());

        Optional<RecipeResponseDto> result = recipeService.getRecipeById(NON_EXISTING_RECIPE_ID);

        assertThat(result).isNotPresent();
    }

    @Test
    public void deleteRecipeByValidId_shouldCallDelete() {

        when(recipeRepository.findById(EXISTING_RECIPE_ID)).thenReturn(Optional.of(entity));

        recipeService.deleteRecipeById(EXISTING_RECIPE_ID);

        verify(recipeRepository, times(1)).deleteById(EXISTING_RECIPE_ID);
        verify(recipeRepository, times(1)).findById(EXISTING_RECIPE_ID);
    }

    @Test
    public void deleteRecipeByInvalidId_shouldThrowException() {

        when(recipeRepository.findById(NON_EXISTING_RECIPE_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> recipeService.deleteRecipeById(NON_EXISTING_RECIPE_ID)).isInstanceOf(RecipeNotFoundException.class);

        verify(recipeRepository, times(1)).findById(NON_EXISTING_RECIPE_ID);
        verify(recipeRepository, never()).deleteById(NON_EXISTING_RECIPE_ID);

    }

    @Test
    public void updateRecipeByValidId_shouldUpdateEntityAndCallSave() {

        requestDto.setName("new name");
        requestDto.setCategory("new category");
        requestDto.setDescription("new description");
        requestDto.setIngredients(List.of("new ingredients"));
        requestDto.setDirections(List.of("new directions"));

        when(recipeRepository.findById(EXISTING_RECIPE_ID)).thenReturn(Optional.of(entity));

        recipeService.updateRecipeById(EXISTING_RECIPE_ID, requestDto);

        assertThat(entity.getName()).isEqualTo("new name");
        assertThat(entity.getCategory()).isEqualTo("new category");
        assertThat(entity.getDescription()).isEqualTo("new description");
        assertThat(entity.getIngredients()).containsExactly("new ingredients");
        assertThat(entity.getDirections()).containsExactly("new directions");

        verify(recipeRepository, times(1)).findById(EXISTING_RECIPE_ID);
        verify(recipeRepository, times(1)).save(entity);
    }

    @Test
    public void updateRecipeByInvalidId_shouldThrowException() {
        when(recipeRepository.findById(NON_EXISTING_RECIPE_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> recipeService.updateRecipeById(NON_EXISTING_RECIPE_ID, requestDto)).isInstanceOf(RecipeNotFoundException.class);

        verify(recipeRepository, times(1)).findById(NON_EXISTING_RECIPE_ID);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    public void searchByValidCategory_shouldReturnEntities() {

        when(recipeRepository.findByCategoryIgnoreCaseOrderByDateDesc(DINNER_CATEGORY)).thenReturn(List.of(entity, anotherEntity));
        when(recipeMapper.toDto(entity)).thenReturn(responseDto);
        when(recipeMapper.toDto(anotherEntity)).thenReturn(anotherResponseDto);

        List<RecipeResponseDto> result = recipeService.searchByCategory(DINNER_CATEGORY);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RecipeResponseDto::getName).containsExactlyInAnyOrder(responseDto.getName(), anotherResponseDto.getName());
        assertThat(result).extracting(RecipeResponseDto::getCategory).containsOnly(DINNER_CATEGORY);

        verify(recipeRepository, times(1)).findByCategoryIgnoreCaseOrderByDateDesc(DINNER_CATEGORY);
        verify(recipeMapper, times(1)).toDto(entity);
        verify(recipeMapper, times(1)).toDto(anotherEntity);

    }

    @Test
    public void searchByInvalidCategory_shouldReturnEmptyList() {

        when(recipeRepository.findByCategoryIgnoreCaseOrderByDateDesc(LUNCH_CATEGORY)).thenReturn(Collections.emptyList());

        List<RecipeResponseDto> result = recipeService.searchByCategory(LUNCH_CATEGORY);

        assertThat(result).isEmpty();
        verify(recipeRepository, times(1)).findByCategoryIgnoreCaseOrderByDateDesc(LUNCH_CATEGORY);
        verify(recipeMapper, never()).toDto(any(Recipe.class));
    }

    @Test
    public void searchByValidName_shouldReturnEntities() {

        when(recipeRepository.findByNameContainingIgnoreCaseOrderByDateDesc(PASTA_SUBSTRING)).thenReturn(Collections.singletonList(entity));
        when(recipeMapper.toDto(entity)).thenReturn(responseDto);

        List<RecipeResponseDto> result = recipeService.searchByName(PASTA_SUBSTRING);

        assertThat(result).hasSize(1);
        assertThat(result).extracting(RecipeResponseDto::getName).containsExactly(responseDto.getName());

        verify(recipeRepository, times(1)).findByNameContainingIgnoreCaseOrderByDateDesc(PASTA_SUBSTRING);
        verify(recipeMapper, times(1)).toDto(entity);
    }

    @Test
    public void searchByInvalidName_shouldReturnEmptyList() {

        when(recipeRepository.findByNameContainingIgnoreCaseOrderByDateDesc(CAKE_SUBSTRING)).thenReturn(Collections.emptyList());

        List<RecipeResponseDto> result = recipeService.searchByName(CAKE_SUBSTRING);

        assertThat(result).isEmpty();
        verify(recipeRepository, times(1)).findByNameContainingIgnoreCaseOrderByDateDesc(CAKE_SUBSTRING);
        verify(recipeMapper, never()).toDto(any(Recipe.class));
    }
}
