package com.gymtracker.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkoutApiTest extends ApiTestBase {

    private static final String W = "/api/v1/workouts";

    @Test
    void startsWorkoutFromTemplate() throws Exception {
        long exercise = weightedExercise(0);
        long template = createTemplate("Верх", exercise);

        long id = idOf(startFromTemplate(template)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Верх"))
                .andExpect(jsonPath("$.startedAt").value(notNullValue()))
                .andExpect(jsonPath("$.finishedAt").value(nullValue()))
                .andExpect(jsonPath("$.exercises", hasSize(1)))
                .andExpect(jsonPath("$.exercises[0].exerciseId").value(exercise))
                .andExpect(jsonPath("$.exercises[0].plannedSets").value(3))
                .andExpect(jsonPath("$.exercises[0].targetSets").value(3))
                .andExpect(jsonPath("$.exercises[0].repsMin").value(8))
                .andExpect(jsonPath("$.exercises[0].sets", hasSize(0))));

        getJson(W + "/active")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        getJson(W + "/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Верх"));
    }

    @Test
    void startsFreeWorkoutWithDefaultName() throws Exception {
        postJson(W, "{\"name\": \"   \"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Свободная тренировка"))
                .andExpect(jsonPath("$.exercises", hasSize(0)));
    }

    @Test
    void startsFreeWorkoutWithOwnName() throws Exception {
        postJson(W, "{\"name\": \"  Кардио \"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Кардио"));
    }

    @Test
    void allowsOnlyOneActiveWorkout() throws Exception {
        postJson(W, "{}").andExpect(status().isCreated());

        postJson(W, "{}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKOUT_ALREADY_ACTIVE"));
    }

    @Test
    void rejectsUnknownTemplate() throws Exception {
        startFromTemplate(999999)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEMPLATE_NOT_FOUND"));

        getJson(W + "/active").andExpect(status().isNoContent());
    }

    @Test
    void activeWorkoutIsEmptyWhenNothingIsRunning() throws Exception {
        getJson(W + "/active")
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void managesExercisesInsideWorkout() throws Exception {
        long id = idOf(postJson(W, "{}"));
        long exercise = weightedExercise(0);
        long other = weightedExercise(1);

        long first = idOf(postJson(W + "/%d/exercises".formatted(id), "{\"exerciseId\": %d}".formatted(exercise))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseId").value(exercise))
                .andExpect(jsonPath("$.targetSets").value(3))
                .andExpect(jsonPath("$.plannedSets").value(nullValue()))
                .andExpect(jsonPath("$.sortOrder").value(0)));

        postJson(W + "/%d/exercises".formatted(id), "{\"exerciseId\": %d, \"targetSets\": 5}".formatted(other))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetSets").value(5))
                .andExpect(jsonPath("$.sortOrder").value(1));

        postJson(W + "/%d/exercises".formatted(id), "{\"exerciseId\": %d}".formatted(exercise))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKOUT_EXERCISE_DUPLICATED"));

        postJson(W + "/%d/exercises".formatted(id), "{\"exerciseId\": 999999}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCISE_NOT_FOUND"));

        patchJson(W + "/%d/exercises/%d".formatted(id, first), "{\"targetSets\": 4}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetSets").value(4));

        deleteJson(W + "/%d/exercises/%d".formatted(id, first)).andExpect(status().isNoContent());

        getJson(W + "/{id}", id)
                .andExpect(jsonPath("$.exercises[*].exerciseId").value(contains((int) other)));

        deleteJson(W + "/%d/exercises/%d".formatted(id, first))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WORKOUT_EXERCISE_NOT_FOUND"));
    }

    @Test
    void managesSets() throws Exception {
        ResultActions workout = startFromTemplate(createTemplate("Верх", weightedExercise(0)));
        long id = idOf(workout);
        long we = firstWorkoutExerciseId(workout);
        String sets = W + "/%d/exercises/%d/sets".formatted(id, we);

        long first = idOf(addSet(id, we, 10, 50, false)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setNumber").value(1))
                .andExpect(jsonPath("$.reps").value(10))
                .andExpect(jsonPath("$.weight").value(50.0))
                .andExpect(jsonPath("$.warmup").value(false)));
        addSet(id, we, 8, 60, false).andExpect(jsonPath("$.setNumber").value(2));
        long third = idOf(addSet(id, we, 15, 20, true)
                .andExpect(jsonPath("$.setNumber").value(3))
                .andExpect(jsonPath("$.warmup").value(true)));

        patchJson(sets + "/" + first, "{\"reps\": 12, \"weight\": 55.5, \"warmup\": false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reps").value(12))
                .andExpect(jsonPath("$.weight").value(55.5));

        deleteJson(sets + "/" + first)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKOUT_SET_NOT_LAST"));

        deleteJson(sets + "/" + third).andExpect(status().isNoContent());

        deleteJson(sets + "/999999")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WORKOUT_SET_NOT_FOUND"));

        getJson(W + "/{id}", id)
                .andExpect(jsonPath("$.exercises[0].sets[*].setNumber").value(contains(1, 2)));
    }

    @Test
    void validatesSets() throws Exception {
        ResultActions workout = startFromTemplate(createTemplate("Верх", weightedExercise(0)));
        long id = idOf(workout);
        long we = firstWorkoutExerciseId(workout);

        postJson(W + "/%d/exercises/%d/sets".formatted(id, we), "{\"reps\": 0, \"weight\": -1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(2)));

        addSet(id, 999999, 10, 50, false)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WORKOUT_EXERCISE_NOT_FOUND"));
    }

    @Test
    void finishesWorkoutWithSummary() throws Exception {
        long exercise = weightedExercise(0);
        ResultActions workout = startFromTemplate(createTemplate("Верх", exercise));
        long id = idOf(workout);
        long we = firstWorkoutExerciseId(workout);
        addSet(id, we, 15, 20, true);
        addSet(id, we, 10, 50, false);
        addSet(id, we, 8, 60, false);

        postJson(W + "/%d/finish".formatted(id), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.finishedAt").value(notNullValue()))
                .andExpect(jsonPath("$.completedSets").value(2))
                .andExpect(jsonPath("$.volume").value(980.0))
                .andExpect(jsonPath("$.completionPercent").value(67));

        getJson(W + "/active").andExpect(status().isNoContent());
        getJson("/api/v1/exercises/{id}", exercise).andExpect(jsonPath("$.workingWeight").value(60.0));

        getJson(W)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].volume").value(980.0));
    }

    @Test
    void cannotFinishWithoutWorkingSets() throws Exception {
        ResultActions workout = startFromTemplate(createTemplate("Верх", weightedExercise(0)));
        long id = idOf(workout);

        postJson(W + "/%d/finish".formatted(id), "")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("WORKOUT_NO_WORKING_SETS"));

        addSet(id, firstWorkoutExerciseId(workout), 15, 20, true);
        postJson(W + "/%d/finish".formatted(id), "").andExpect(status().isUnprocessableContent());

        getJson(W + "/active").andExpect(status().isOk());
    }

    @Test
    void finishedWorkoutIsReadOnly() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        long id = finishedWorkoutFromTemplate(template, 10, 50);
        long we = ((Number) read(getJson(W + "/{id}", id), "$.exercises[0].id")).longValue();

        addSet(id, we, 10, 50, false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKOUT_ALREADY_FINISHED"));
        postJson(W + "/%d/finish".formatted(id), "").andExpect(status().isConflict());
        deleteJson(W + "/" + id).andExpect(status().isConflict());
    }

    @Test
    void cancelsActiveWorkout() throws Exception {
        long id = idOf(postJson(W, "{}"));

        deleteJson(W + "/" + id).andExpect(status().isNoContent());

        getJson(W + "/{id}", id)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WORKOUT_NOT_FOUND"));
        getJson(W + "/active").andExpect(status().isNoContent());
        getJson(W).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void historyIsNewestFirstAndLimited() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        long older = finishedWorkoutFromTemplate(template, 10, 50);
        long newer = finishedWorkoutFromTemplate(template, 10, 55);

        getJson(W)
                .andExpect(jsonPath("$[*].id").value(contains((int) newer, (int) older)));
        getJson(W + "?limit=1")
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(newer));
        getJson(W + "?finished=false")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("WORKOUT_FILTER_UNSUPPORTED"));
    }

    @Test
    void unknownWorkoutIsNotFound() throws Exception {
        getJson(W + "/999999").andExpect(status().isNotFound());
        deleteJson(W + "/999999").andExpect(status().isNotFound());
        postJson(W + "/999999/finish", "").andExpect(status().isNotFound());
    }
}
