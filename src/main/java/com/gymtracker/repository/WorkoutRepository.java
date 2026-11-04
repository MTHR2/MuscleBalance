package com.gymtracker.repository;

import com.gymtracker.dto.stats.MuscleLoadRow;
import com.gymtracker.entity.Workout;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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

    // статистика: неделя это окно [from, to) по началу тренировки; считаются рабочие подходы
    // и завершённых, и идущей тренировки (карта обновляется по ходу), по одной строке на пару
    // (мышца, роль), коэффициенты применяет сервис
    @Query("""
            select new com.gymtracker.dto.stats.MuscleLoadRow(m.id, em.muscleRole, count(s), sum(s.reps))
            from Workout w
              join w.exercises we
              join we.sets s
              join we.exercise e
              join e.muscles em
              join em.muscle m
            where w.startedAt >= :from and w.startedAt < :to
              and s.warmup = false
            group by m.id, em.muscleRole
            """)
    List<MuscleLoadRow> findMuscleLoad(@Param("from") OffsetDateTime from,
                                       @Param("to") OffsetDateTime to);

    // сколько тренировок завершено за окно
    @Query("""
            select count(w) from Workout w
            where w.finishedAt is not null
              and w.startedAt >= :from and w.startedAt < :to
            """)
    long countFinishedBetween(@Param("from") OffsetDateTime from,
                              @Param("to") OffsetDateTime to);

    // сколько рабочих подходов сделано за окно, включая идущую тренировку
    // (реальное число, без 0.5 для вторичных мышц)
    @Query("""
            select count(s) from Workout w
              join w.exercises we
              join we.sets s
            where w.startedAt >= :from and w.startedAt < :to
              and s.warmup = false
            """)
    long countWorkingSetsBetween(@Param("from") OffsetDateTime from,
                                 @Param("to") OffsetDateTime to);

    // завершена ли за окно хотя бы одна тренировка по этому шаблону (для признака "сегодня уже сделано")
    @Query("""
            select count(w) from Workout w
            where w.workoutTemplate.id = :templateId
              and w.finishedAt is not null
              and w.startedAt >= :from and w.startedAt < :to
            """)
    long countFinishedFromTemplateBetween(@Param("templateId") Long templateId,
                                          @Param("from") OffsetDateTime from,
                                          @Param("to") OffsetDateTime to);

    // история: завершённые тренировки, свежие первыми
    List<Workout> findByFinishedAtIsNotNullOrderByFinishedAtDesc(Pageable pageable);

}
