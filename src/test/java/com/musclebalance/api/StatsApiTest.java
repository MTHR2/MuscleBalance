package com.musclebalance.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatsApiTest extends ApiTestBase {

    private static final String S = "/api/v1/stats";

    @Test
    void emptyDatabaseGivesZeroStats() throws Exception {
        getJson(S + "/overview")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutsThisMonth").value(0))
                .andExpect(jsonPath("$.volumeTonnes30d").value(0.0));
        getJson(S + "/week")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutsDone").value(0))
                .andExpect(jsonPath("$.workingSets").value(0));
        getJson(S + "/muscles")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].sets", everyItem(is(0.0))));
        getJson(S + "/muscle-groups")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].sets", everyItem(is(0.0))));
        getJson(S + "/exercises").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        getJson(S + "/records")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(0)))
                .andExpect(jsonPath("$.totalInPeriod").value(0));
    }

    @Test
    void monthlyWorkoutsReturnsRequestedNumberOfMonths() throws Exception {
        getJson(S + "/workouts/monthly").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(12)));
        getJson(S + "/workouts/monthly?months=3").andExpect(jsonPath("$", hasSize(3)));
        getJson(S + "/workouts/monthly?months=61")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("months"));
    }

    @Test
    void finishedWorkoutShowsUpInStats() throws Exception {
        long exercise = weightedExercise(0);
        List<Map<String, Object>> exerciseMuscles =
                read(getJson("/api/v1/exercises/{id}", exercise), "$.muscles[?(@.role == 'primary')]");
        int primaryMuscle = ((Number) exerciseMuscles.getFirst().get("muscleId")).intValue();
        long template = createTemplate("Верх", exercise);
        finishedWorkoutFromTemplate(template, 10, 100);

        getJson(S + "/overview")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutsThisMonth").value(1))
                .andExpect(jsonPath("$.volumeTonnes30d").value(1.0))
                .andExpect(jsonPath("$.avgSets").value(1.0));

        getJson(S + "/week")
                .andExpect(jsonPath("$.workoutsDone").value(1))
                .andExpect(jsonPath("$.workingSets").value(1));

        getJson(S + "/muscles")
                .andExpect(jsonPath("$[?(@.muscleId == %d)].sets".formatted(primaryMuscle)).value(hasItem(1.0)))
                .andExpect(jsonPath("$[?(@.muscleId == %d)].reps".formatted(primaryMuscle)).value(hasItem(10.0)));

        getJson(S + "/muscle-groups")
                .andExpect(jsonPath("$[*].sets").value(hasItem(greaterThanOrEqualTo(1.0))));

        getJson(S + "/workouts/monthly?months=1")
                .andExpect(jsonPath("$[0].count").value(1));

        getJson(S + "/exercises")
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(exercise))
                .andExpect(jsonPath("$[0].workingWeight").value(100.0));
    }

    @Test
    void exerciseProgressTracksBestSetPerWeek() throws Exception {
        long exercise = weightedExercise(0);
        long template = createTemplate("Верх", exercise);
        finishedWorkoutFromTemplate(template, 10, 100);

        getJson(S + "/exercises/{id}/progress", exercise)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseId").value(exercise))
                .andExpect(jsonPath("$.loadType").value("weighted"))
                .andExpect(jsonPath("$.points", hasSize(1)))
                .andExpect(jsonPath("$.points[0].weight").value(100.0))
                .andExpect(jsonPath("$.points[0].reps").value(10))
                .andExpect(jsonPath("$.current").value(100.0))
                .andExpect(jsonPath("$.estimatedMax").value(greaterThan(100.0)));

        getJson(S + "/exercises/{id}/progress?period=YEAR", exercise)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points", hasSize(1)));
    }

    @Test
    void exerciseProgressWithoutSetsIsEmpty() throws Exception {
        getJson(S + "/exercises/{id}/progress", weightedExercise(0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points", hasSize(0)));
    }

    @Test
    void exerciseProgressForUnknownExerciseIsNotFound() throws Exception {
        getJson(S + "/exercises/999999/progress")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCISE_NOT_FOUND"));
    }

    @Test
    void recordsListsHeavierSets() throws Exception {
        long exercise = weightedExercise(0);
        long template = createTemplate("Верх", exercise);
        finishedWorkoutFromTemplate(template, 10, 100);
        finishedWorkoutFromTemplate(template, 10, 110);

        getJson(S + "/records?period=YEAR&limit=5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.totalInPeriod").value(greaterThanOrEqualTo(0)));
    }

    @Test
    void rejectsUnknownPeriod() throws Exception {
        getJson(S + "/overview?period=WEEK")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void weekAcceptsExplicitStart() throws Exception {
        getJson(S + "/week?weekStart=2026-01-05")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutsDone").value(0));
        getJson(S + "/muscles?weekStart=2026-01-05")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(notNullValue()));
        getJson(S + "/week?weekStart=not-a-date")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
