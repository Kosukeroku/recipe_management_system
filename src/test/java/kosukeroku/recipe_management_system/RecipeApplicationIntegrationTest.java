package kosukeroku.recipe_management_system;

import jakarta.persistence.EntityManager;
import kosukeroku.recipe_management_system.dto.RecipeRequestDto;
import kosukeroku.recipe_management_system.dto.RecipeResponseDto;
import kosukeroku.recipe_management_system.model.Recipe;
import kosukeroku.recipe_management_system.repository.RecipeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecipeApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        recipeRepository.deleteAll();
    }

    private final String BASE_URL = "/api/recipe";
    private RecipeRequestDto requestDto = new RecipeRequestDto(
            "Pasta Carbonara",
            "dinner",
            "Creamy Italian pasta with bacon",
            List.of("spaghetti", "eggs", "bacon", "parmesan"),
            List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all")
    );

    @Test
    @Transactional
    public void fullCRUDCycle_ShouldWork() {

        //create
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(BASE_URL + "/new", requestDto, Map.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long id = Long.valueOf(createResponse.getBody().get("id").toString());
        assertThat(id).isNotNull();

        Optional<Recipe> savedRecipe = recipeRepository.findById(id);
        assertThat(savedRecipe).isPresent();
        assertThat(savedRecipe.get().getName()).isEqualTo("Pasta Carbonara");
        assertThat(savedRecipe.get().getCategory()).isEqualTo("dinner");

        //read
        ResponseEntity<RecipeResponseDto> readResponse = restTemplate.getForEntity(BASE_URL + "/" + id, RecipeResponseDto.class);

        RecipeResponseDto expectedResponse = new RecipeResponseDto(
                "Pasta Carbonara",
                "dinner",
                LocalDateTime.now().toString(),
                "Creamy Italian pasta with bacon",
                List.of("spaghetti", "eggs", "bacon", "parmesan"),
                List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all")
        );

        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readResponse.getBody())
                .usingRecursiveComparison()
                .ignoringFields("date")
                .isEqualTo(expectedResponse);

        //update
        RecipeRequestDto updateRequest = new RecipeRequestDto(
                "Updated pasta Carbonara",
                "dinner",
                requestDto.getDescription(),
                requestDto.getIngredients(),
                requestDto.getDirections()
        );

        ResponseEntity<Void> updateResponse = restTemplate.exchange(
                BASE_URL + "/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Void.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        entityManager.flush();
        entityManager.clear();

        //check update
        Recipe expectedUpdatedRecipe = new Recipe(
                id,
                "Updated pasta Carbonara",
                "dinner",
                LocalDateTime.now(),
                "Creamy Italian pasta with bacon",
                List.of("spaghetti", "eggs", "bacon", "parmesan"),
                List.of("Cook pasta", "Fry bacon", "Mix eggs with cheese", "Combine all")
        );

        Optional<Recipe> updatedRecipe = recipeRepository.findById(id);

        assertThat(updatedRecipe).isPresent();
        assertThat(updatedRecipe.get())
                .usingRecursiveComparison()
                .ignoringFields("id", "date")
                .isEqualTo(expectedUpdatedRecipe);


        //delete
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL + "/" + id,
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        entityManager.flush();
        entityManager.clear();

        //check delete
        Optional<Recipe> deletedRecipe = recipeRepository.findById(id);
        assertThat(deletedRecipe).isEmpty();

        ResponseEntity<Void> checkDeleteResponse = restTemplate.getForEntity(BASE_URL + "/" + id, Void.class);

        assertThat(checkDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void searchByCategory_ShouldReturnFilteredResults() throws InterruptedException {
        Recipe recipe1 = createTestRecipeWithoutDate("Pasta Carbonara", "dinner");
        recipe1.setDate(LocalDateTime.now().minusDays(2));
        recipeRepository.save(recipe1);


        Recipe recipe2 = createTestRecipeWithoutDate("Greek Salad", "dinner");
        recipe2.setDate(LocalDateTime.now().minusDays(1));
        recipeRepository.save(recipe2);


        Recipe recipe3 = createTestRecipeWithoutDate("Chocolate Cake", "dessert");
        recipe3.setDate(LocalDateTime.now());
        recipeRepository.save(recipe3);

        ResponseEntity<RecipeResponseDto[]> response = restTemplate.getForEntity(
                BASE_URL + "/search?category=dinner",
                RecipeResponseDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].getName()).isEqualTo("Greek Salad");
        assertThat(response.getBody()[1].getName()).isEqualTo("Pasta Carbonara");

    }

    @Test
    void searchByName_ShouldReturnFilteredResults() {
        Recipe recipe1 = createTestRecipeWithoutDate("Pasta Carbonara", "dinner");
        recipe1.setDate(LocalDateTime.now().minusDays(2));
        recipeRepository.save(recipe1);


        Recipe recipe2 = createTestRecipeWithoutDate("Pasta Bolognese", "dinner");
        recipe2.setDate(LocalDateTime.now().minusDays(1));
        recipeRepository.save(recipe2);


        Recipe recipe3 = createTestRecipeWithoutDate("Chocolate Cake", "dessert");
        recipe3.setDate(LocalDateTime.now());
        recipeRepository.save(recipe3);

        ResponseEntity<RecipeResponseDto[]> response = restTemplate.getForEntity(
                BASE_URL + "/search?name=pasta",
                RecipeResponseDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].getName()).isEqualTo("Pasta Bolognese");
        assertThat(response.getBody()[1].getName()).isEqualTo("Pasta Carbonara");
    }

    @Test
    void searchByNonPresentField_ShouldReturnEmptyList() {
        Recipe recipe1 = createTestRecipeWithoutDate("Pasta Carbonara", "dinner");
        recipe1.setDate(LocalDateTime.now().minusDays(2));
        recipeRepository.save(recipe1);


        Recipe recipe2 = createTestRecipeWithoutDate("Greek Salad", "dinner");
        recipe2.setDate(LocalDateTime.now().minusDays(1));
        recipeRepository.save(recipe2);


        Recipe recipe3 = createTestRecipeWithoutDate("Chocolate Cake", "dessert");
        recipe3.setDate(LocalDateTime.now());
        recipeRepository.save(recipe3);

        ResponseEntity<RecipeResponseDto[]> response = restTemplate.getForEntity(
                BASE_URL + "/search?name=pizza",
                RecipeResponseDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(0);
    }

    @Test
    void searchByBothParameters_ShouldReturn400() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                BASE_URL + "/search?name=pasta&category=dinner",
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void searchWithoutParameters_ShouldReturn400() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                BASE_URL + "/search",
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    Recipe createTestRecipeWithoutDate(String name, String category) {

        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setCategory(category);
        recipe.setDescription("Description");
        recipe.setIngredients(List.of("ing1", "ing2"));
        recipe.setDirections(List.of("step1", "step2"));

        return recipe;
    }

    @Test
    void getNonExistentRecipe_ShouldReturn404() {
        ResponseEntity<Void> response = restTemplate.getForEntity(BASE_URL + "/999", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createRecipeWithEmptyName_ShouldReturn400() {
        RecipeRequestDto invalidRequest = new RecipeRequestDto(
                "",
                "dinner",
                "Valid description",
                List.of("ingredient1", "ingredient2"),
                List.of("step1", "step2")
        );

        ResponseEntity<Void> response = restTemplate.postForEntity(
                BASE_URL + "/new",
                invalidRequest,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(recipeRepository.findAll()).isEmpty();
    }
}