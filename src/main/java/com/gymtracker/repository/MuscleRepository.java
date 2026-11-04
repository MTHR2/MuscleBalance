package com.gymtracker.repository;

import com.gymtracker.entity.Muscle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MuscleRepository extends JpaRepository<Muscle,Short> {

    // все мышцы для статистики: по группам, внутри группы по порядку;
    // группу грузим сразу, чтобы взять название без запроса на каждую мышцу
    @EntityGraph(attributePaths = "muscleGroup")
    List<Muscle> findAllByOrderByMuscleGroup_SortOrderAscSortOrderAsc();

}
