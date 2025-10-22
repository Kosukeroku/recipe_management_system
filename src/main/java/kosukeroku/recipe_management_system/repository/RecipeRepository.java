package kosukeroku.recipe_management_system.repository;

import kosukeroku.recipe_management_system.model.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @EntityGraph(attributePaths = {"author"})
    @Override
    List<Recipe> findAll();

    @EntityGraph(attributePaths = {"author"})
    @Override
    Optional<Recipe> findById(Long id);

    @EntityGraph(attributePaths = {"author"})
    List<Recipe> findByCategoryIgnoreCaseOrderByDateDesc(String category);

    @EntityGraph(attributePaths = {"author"})
    List<Recipe> findByNameContainingIgnoreCaseOrderByDateDesc(String category);
}