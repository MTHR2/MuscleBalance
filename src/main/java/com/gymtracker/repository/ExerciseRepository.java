package com.gymtracker.repository;


import com.gymtracker.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // упражнения, у которых основная мышца входит в группу; удалённые не показываем
    @Query("""
            select distinct e from Exercise e
            join e.muscles em
            where em.muscle.muscleGroup.id = :muscleGroupId
              and em.muscleRole = com.gymtracker.entity.MuscleRole.primary
              and e.archivedAt is null
            order by e.name
            """)
    List<Exercise> findByPrimaryMuscleGroup(@Param("muscleGroupId") Short muscleGroupId);


}
