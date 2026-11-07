package com.musclebalance.repository;

import com.musclebalance.entity.WorkoutTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

    @EntityGraph(attributePaths = "exercises")
    List<WorkoutTemplate> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"exercises", "exercises.exercise"})
    Optional<WorkoutTemplate> findWithExercisesById(Long id);

    boolean existsByNameIgnoreCase(String name);
}
