package kosukeroku.recipe_management_system;


import kosukeroku.recipe_management_system.controller.RecipeController;
import kosukeroku.recipe_management_system.dto.RecipeRequestDto;
import kosukeroku.recipe_management_system.dto.RecipeResponseDto;
import kosukeroku.recipe_management_system.exception.RecipeNotFoundException;
import kosukeroku.recipe_management_system.model.Recipe;
import kosukeroku.recipe_management_system.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    private static final String VALID_JSON = """
            {
                "name": "Pasta Carbonara",
                "category": "dinner",
                "description": "Creamy Italian pasta with bacon",
                "ingredients": ["spaghetti", "eggs", "bacon", "parmesan"],
                "directions": ["Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all"]
            }
            """;
    private static final String VALID_JSON_ARRAY = "[ " + VALID_JSON + " ]";


    private static final String INVALID_JSON_WITH_MISSING_NAME = """
            {
                "name": "",
                "category": "dinner",
                "description": "Creamy Italian pasta with bacon",
                "ingredients": ["spaghetti", "eggs", "bacon", "parmesan"],
                "directions": ["Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all"]
            }
            """;

    private Recipe entity;
    private RecipeResponseDto responseDto;
    private static final Long EXISTING_RECIPE_ID = 1L;
    private static final Long NON_EXISTING_RECIPE_ID = 999L;
    private static final Long ANOTHER_RECIPE_ID = 2L;

    @BeforeEach
    public void setUp() throws Exception {
        entity = new Recipe();
        entity.setName("Pasta Carbonara");
        entity.setCategory("dinner");
        entity.setDescription("Creamy Italian pasta with bacon");
        entity.setIngredients(List.of("spaghetti", "eggs", "bacon", "parmesan"));
        entity.setDirections(List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all"));
        entity.setDate(LocalDateTime.now());

        responseDto = new RecipeResponseDto(
                "Pasta Carbonara",
                "dinner",
                LocalDateTime.now().toString(),
                "Creamy Italian pasta with bacon",
                List.of("spaghetti", "eggs", "bacon", "parmesan"),
                List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all")
        );

    }


    @Test
    public void addValidRecipe_ShouldReturnId() throws Exception {

        when(recipeService.saveRecipe(any(RecipeRequestDto.class))).thenReturn(EXISTING_RECIPE_ID);

        mockMvc.perform(post("/api/recipe/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXISTING_RECIPE_ID.toString()));
    }

    @Test
    public void addInvalidRecipe_ShouldReturn400() throws Exception {

        mockMvc.perform(post("/api/recipe/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_JSON_WITH_MISSING_NAME))
                .andExpect(status().isBadRequest());

        verify(recipeService, never()).saveRecipe(any(RecipeRequestDto.class));
    }

    @Test
    public void getRecipeWithValidId_ShouldReturnValidDto() throws Exception {

        when(recipeService.getRecipeById(EXISTING_RECIPE_ID)).thenReturn(Optional.of(responseDto));

        mockMvc.perform(get("/api/recipe/{id}", EXISTING_RECIPE_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(VALID_JSON));
    }

    @Test
    public void getRecipeWithInvalidId_ShouldReturn404() throws Exception {

        when(recipeService.getRecipeById(NON_EXISTING_RECIPE_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recipe/{id}", NON_EXISTING_RECIPE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteRecipeWithValidId_ShouldReturn204() throws Exception {

        mockMvc.perform(delete("/api/recipe/{id}", EXISTING_RECIPE_ID))
                .andExpect(status().isNoContent());

        verify(recipeService, times(1)).deleteRecipeById(EXISTING_RECIPE_ID);
    }

    @Test
    public void deleteRecipeWithInvalidId_ShouldReturn404() throws Exception {

        doThrow(new RecipeNotFoundException(NON_EXISTING_RECIPE_ID))
                .when(recipeService).deleteRecipeById(NON_EXISTING_RECIPE_ID);

        mockMvc.perform(delete("/api/recipe/{id}", NON_EXISTING_RECIPE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateRecipeWithValidIdAndValidDto_ShouldReturnUpdated204() throws Exception {
        mockMvc.perform(put("/api/recipe/{id}", EXISTING_RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isNoContent());

        verify(recipeService, times(1)).updateRecipeById(eq(EXISTING_RECIPE_ID), any(RecipeRequestDto.class));
    }

    @Test
    public void updateRecipeWithInvalidIdAndValidDto_ShouldReturn404() throws Exception {

        doThrow(new RecipeNotFoundException(NON_EXISTING_RECIPE_ID))
                .when(recipeService).updateRecipeById(eq(NON_EXISTING_RECIPE_ID), any(RecipeRequestDto.class));

        mockMvc.perform(put("/api/recipe/{id}", NON_EXISTING_RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateRecipeWithValidIdAndInvalidDto_ShouldReturn400() throws Exception {

        mockMvc.perform(put("/api/recipe/{id}", EXISTING_RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_JSON_WITH_MISSING_NAME))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void searchRecipeByValidCategory_ShouldReturnValidList() throws Exception {

        List<RecipeResponseDto> recipes = List.of(responseDto);
        when(recipeService.searchByCategory("dinner")).thenReturn(recipes);

        mockMvc.perform(get("/api/recipe/search")
                        .param("category", "dinner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(VALID_JSON_ARRAY));
    }

    @Test
    public void searchRecipeByValidName_ShouldReturnValidList() throws Exception {
        List<RecipeResponseDto> recipes = List.of(responseDto);
        when(recipeService.searchByName("pasta")).thenReturn(recipes);

        mockMvc.perform(get("/api/recipe/search")
                        .param("name", "pasta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().json(VALID_JSON_ARRAY));
    }

    @Test
    public void searchRecipeByNonExistentCategory_ShouldReturnEmptyList() throws Exception {

        when(recipeService.searchByCategory("lunch")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/recipe/search")
                        .param("category", "lunch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void searchRecipeByNonExistentName_ShouldReturnEmptyList() throws Exception {
        when(recipeService.searchByName("cake")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/recipe/search")
                        .param("name", "cake"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void searchRecipeWithoutParameters_ShouldReturn400() throws Exception {

        mockMvc.perform(get("/api/recipe/search")
                .param("category", "dinner")
                .param("name", "pasta"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void searchRecipeWithBothParameters_ShouldReturn400() throws Exception {

        mockMvc.perform(get("/api/recipe/search"))
                .andExpect(status().isBadRequest());
    }


}
