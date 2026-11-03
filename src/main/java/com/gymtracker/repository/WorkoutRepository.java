package com.gymtracker.repository;

import com.gymtracker.entity.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    // идёт ли тренировка сейчас (в БД незавершённая максимум одна)
    boolean existsByFinishedAtIsNull();

    // текущая тренировка: упражнения вместе с самими Exercise (названия, картинки, рабочий вес)
    // подходы (sets) сюда не входят: два вложенных List нельзя грузить одним графом
    @EntityGraph(attributePaths = {"exercises", "exercises.exercise"})
    Optional<Workout> findByFinishedAtIsNull();

    // тренировка по id, с теми же упражнениями
    @EntityGraph(attributePaths = {"exercises", "exercises.exercise"})
    Optional<Workout> findWithExercisesById(Long id);


}
