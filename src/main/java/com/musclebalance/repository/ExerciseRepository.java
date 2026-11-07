package com.musclebalance.repository;


import com.musclebalance.entity.Exercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // каталог: все фильтры необязательные (null = не фильтровать); удалённые не показываем.
    // группа: у упражнения есть основная мышца из этой группы. q: подстрока в названии без учёта регистра,
    // пустая строка совпадает со всем. Мышцы грузим сразу, чтобы не ходить в БД за каждой.
    @EntityGraph(attributePaths = {"muscles", "muscles.muscle"})
    @Query("""
            select e from Exercise e
            where e.archivedAt is null
              and lower(e.name) like lower(concat('%', :q, '%'))
              and (cast(:favourite as boolean) is null or e.favourite = :favourite)
              and (cast(:custom as boolean) is null or e.custom = :custom)
              and (cast(:groupId as short) is null or exists (
                    select 1 from ExerciseMuscle em
                    where em.exercise = e
                      and em.muscleRole = com.musclebalance.entity.MuscleRole.primary
                      and em.muscle.muscleGroup.id = :groupId))
            order by e.name
            """)
    List<Exercise> search(@Param("q") String q,
                          @Param("groupId") Short groupId,
                          @Param("favourite") Boolean favourite,
                          @Param("custom") Boolean custom);

    // название занято среди неудалённых (в БД уникальный индекс по lower(name))
    boolean existsByNameIgnoreCaseAndArchivedAtIsNull(String name);

}
