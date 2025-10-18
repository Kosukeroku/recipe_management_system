package kosukeroku.recipe_management_system;

import jakarta.persistence.EntityManager;
import kosukeroku.recipe_management_system.dto.RecipeRequestDto;
import kosukeroku.recipe_management_system.dto.RecipeResponseDto;
import kosukeroku.recipe_management_system.model.Recipe;
import kosukeroku.recipe_management_system.repository.RecipeRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
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
                "lalal",
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
                "lalal",
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
    @Transactional
    void searchByCategory_ShouldReturnFilteredResults() {
        Recipe recipe1 = new Recipe();
        recipe1.setName("Pasta Carbonara");
        recipe1.setCategory("dinner");
        recipe1.setDescription("Description");
        recipe1.setIngredients(List.of("ing1", "ing2"));
        recipe1.setDirections(List.of("step1", "step2"));
        recipeRepository.save(recipe1);

        Recipe recipe2 = new Recipe();
        recipe2.setName("Greek Salad");
        recipe2.setCategory("dinner");
        recipe2.setDescription("Description");
        recipe2.setIngredients(List.of("ing1", "ing2"));
        recipe2.setDirections(List.of("step1", "step2"));
        recipeRepository.save(recipe2);

        Recipe recipe3 = new Recipe();
        recipe3.setName("Chocolate Cake");
        recipe3.setCategory("dessert");
        recipe3.setDescription("Description");
        recipe3.setIngredients(List.of("ing1", "ing2"));
        recipe3.setDirections(List.of("step1", "step2"));
        recipeRepository.save(recipe3);

    }


}