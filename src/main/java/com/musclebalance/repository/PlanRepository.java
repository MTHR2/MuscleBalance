package com.musclebalance.repository;

import com.musclebalance.entity.Plan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    // список планов: активный первым, остальные по алфавиту;
    // дни грузим сразу, чтобы показать число тренировочных дней без N+1
    @EntityGraph(attributePaths = "days")
    List<Plan> findAllByOrderByActiveDescNameAsc();

    boolean existsByNameIgnoreCase(String name);

    // карточка плана: дни вместе с тренировками из библиотеки
    @EntityGraph(attributePaths = {"days", "days.workoutTemplate"})
    Optional<Plan> findWithDaysById(Long id);

    // активный план (в БД он максимум один)
    Optional<Plan> findByActiveTrue();

    // стоит ли тренировка из библиотеки хотя бы в одном плане (перед удалением)
    boolean existsByDays_WorkoutTemplate_Id(Long workoutTemplateId);

    // в каких планах стоит тренировка (чтобы показать в сообщении о запрете удаления)
    List<Plan> findDistinctByDays_WorkoutTemplate_Id(Long workoutTemplateId);
}
