package com.gymtracker.repository;

import com.gymtracker.entity.Workout;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends org.springframework.data.repository.Repository<Workout, Long> {

    interface PeriodCountRow {
        LocalDate getPeriodStart();

        long getCount();
    }

    interface BestSetRow {
        LocalDate getWeekStart();

        BigDecimal getWeight();

        short getReps();
    }

    interface RecordRow {
        Long getExerciseId();

        String getName();

        String getLoadType();

        LocalDate getDate();

        BigDecimal getWeight();

        short getReps();

        BigDecimal getDelta();
    }

    @Query(nativeQuery = true, value = """
            SELECT date_trunc('week', w.started_at AT TIME ZONE :tz)::date AS "periodStart",
                   count(*) AS "count"
            FROM workouts w
            WHERE w.finished_at IS NOT NULL
            GROUP BY 1
            ORDER BY 1
            """)
    List<PeriodCountRow> countFinishedByWeek(@Param("tz") String tz);

    @Query(nativeQuery = true, value = """
            SELECT date_trunc('month', w.started_at AT TIME ZONE :tz)::date AS "periodStart",
                   count(*) AS "count"
            FROM workouts w
            WHERE w.finished_at IS NOT NULL
              AND w.started_at >= :from AND w.started_at < :to
            GROUP BY 1
            ORDER BY 1
            """)
    List<PeriodCountRow> countFinishedByMonth(@Param("from") OffsetDateTime from,
                                              @Param("to") OffsetDateTime to,
                                              @Param("tz") String tz);

    @Query(nativeQuery = true, value = """
            SELECT avg(extract(epoch FROM w.finished_at - w.started_at))
            FROM workouts w
            WHERE w.finished_at IS NOT NULL
              AND w.started_at >= :from AND w.started_at < :to
            """)
    Double avgDurationSeconds(@Param("from") OffsetDateTime from,
                              @Param("to") OffsetDateTime to);

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT ON (1)
                   date_trunc('week', w.started_at AT TIME ZONE :tz)::date AS "weekStart",
                   s.weight AS "weight",
                   s.reps AS "reps"
            FROM workout_sets s
            JOIN workout_exercises we ON we.id = s.workout_exercise_id
            JOIN workouts w ON w.id = we.workout_id
            JOIN exercises e ON e.id = we.exercise_id
            WHERE we.exercise_id = :exerciseId
              AND NOT s.is_warmup
              AND w.finished_at IS NOT NULL
              AND w.started_at >= :from AND w.started_at < :to
            ORDER BY 1,
                     CASE WHEN e.load_type = 'bodyweight' THEN s.reps ELSE s.weight END DESC,
                     CASE WHEN e.load_type = 'bodyweight' THEN s.weight ELSE s.reps END DESC
            """)
    List<BestSetRow> findBestSetByWeek(@Param("exerciseId") Long exerciseId,
                                       @Param("from") OffsetDateTime from,
                                       @Param("to") OffsetDateTime to,
                                       @Param("tz") String tz);

    @Query(nativeQuery = true, value = """
            SELECT max(CASE WHEN e.load_type = 'bodyweight' THEN s.reps ELSE s.weight END)
            FROM workout_sets s
            JOIN workout_exercises we ON we.id = s.workout_exercise_id
            JOIN workouts w ON w.id = we.workout_id
            JOIN exercises e ON e.id = we.exercise_id
            WHERE we.exercise_id = :exerciseId
              AND NOT s.is_warmup
              AND w.finished_at IS NOT NULL
              AND w.started_at < :before
            """)
    BigDecimal findBestMetricBefore(@Param("exerciseId") Long exerciseId,
                                    @Param("before") OffsetDateTime before);

    @Query(nativeQuery = true, value = """
            WITH best AS (
                SELECT DISTINCT ON (we.id)
                       we.exercise_id,
                       w.started_at,
                       s.weight,
                       s.reps,
                       CASE WHEN e.load_type = 'bodyweight' THEN s.reps ELSE s.weight END AS metric
                FROM workout_sets s
                JOIN workout_exercises we ON we.id = s.workout_exercise_id
                JOIN workouts w ON w.id = we.workout_id
                JOIN exercises e ON e.id = we.exercise_id
                WHERE NOT s.is_warmup
                  AND w.finished_at IS NOT NULL
                  AND w.started_at < :to
                ORDER BY we.id,
                         metric DESC,
                         CASE WHEN e.load_type = 'bodyweight' THEN s.weight ELSE s.reps END DESC
            ),
            ranked AS (
                SELECT b.*,
                       max(b.metric) OVER (PARTITION BY b.exercise_id ORDER BY b.started_at
                                           ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) AS prev_best
                FROM best b
            )
            SELECT r.exercise_id AS "exerciseId",
                   e.name AS "name",
                   e.load_type AS "loadType",
                   (r.started_at AT TIME ZONE :tz)::date AS "date",
                   r.weight AS "weight",
                   r.reps AS "reps",
                   r.metric - r.prev_best AS "delta"
            FROM ranked r
            JOIN exercises e ON e.id = r.exercise_id
            WHERE r.prev_best IS NOT NULL
              AND r.metric > r.prev_best
              AND r.started_at >= :from
            ORDER BY r.started_at DESC, e.name
            """)
    List<RecordRow> findRecords(@Param("from") OffsetDateTime from,
                                @Param("to") OffsetDateTime to,
                                @Param("tz") String tz);
}
