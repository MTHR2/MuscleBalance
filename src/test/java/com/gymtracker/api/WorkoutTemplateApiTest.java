package com.gymtracker.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkoutTemplateApiTest extends ApiTestBase {

    @Test
    void createsTemplateKeepingExerciseOrder() throws Exception {
        long first = weightedExercise(0);
        long second = weightedExercise(1);

        long id = idOf(postJson("/api/v1/workout-templates", """
                {"name": "Верх", "exercises": [
                  {"exerciseId": %d, "targetSets": 4, "repsMin": 6, "repsMax": 8},
                  {"exerciseId": %d, "targetSets": 3}
                ]}
                """.formatted(first, second))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Верх"))
                .andExpect(jsonPath("$.exercises[*].exerciseId").value(contains((int) first, (int) second)))
                .andExpect(jsonPath("$.exercises[0].targetSets").value(4))
                .andExpect(jsonPath("$.exercises[0].repsMin").value(6))
                .andExpect(jsonPath("$.exercises[0].repsMax").value(8))
                .andExpect(jsonPath("$.exercises[0].exerciseName").isNotEmpty()));

        getJson("/api/v1/workout-templates/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises", hasSize(2)));

        getJson("/api/v1/workout-templates")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].exerciseCount").value(2));
    }

    @Test
    void listsTemplatesByName() throws Exception {
        long exercise = weightedExercise(0);
        createTemplate("Ноги", exercise);
        createTemplate("Грудь", exercise);

        getJson("/api/v1/workout-templates")
                .andExpect(jsonPath("$[*].name").value(contains("Грудь", "Ноги")));
    }

    @Test
    void rejectsDuplicateNameIgnoringCase() throws Exception {
        createTemplate("Верх", weightedExercise(0));

        postJson("/api/v1/workout-templates", """
                {"name": "верх", "exercises": [{"exerciseId": %d, "targetSets": 3}]}
                """.formatted(weightedExercise(0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TEMPLATE_NAME_TAKEN"));
    }

    @Test
    void rejectsSameExerciseTwice() throws Exception {
        long exercise = weightedExercise(0);

        postJson("/api/v1/workout-templates", """
                {"name": "Верх", "exercises": [
                  {"exerciseId": %d, "targetSets": 3}, {"exerciseId": %d, "targetSets": 3}
                ]}
                """.formatted(exercise, exercise))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("TEMPLATE_EXERCISE_DUPLICATED"));
    }

    @Test
    void rejectsUnknownExercise() throws Exception {
        postJson("/api/v1/workout-templates", """
                {"name": "Верх", "exercises": [{"exerciseId": 999999, "targetSets": 3}]}
                """)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("EXERCISES_NOT_FOUND"));
    }

    @Test
    void validatesRequestBody() throws Exception {
        postJson("/api/v1/workout-templates", """
                {"name": "", "exercises": []}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("exercises")));

        postJson("/api/v1/workout-templates", """
                {"name": "Верх", "exercises": [{"exerciseId": %d, "targetSets": 0}]}
                """.formatted(weightedExercise(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("exercises[0].targetSets"));
    }

    @Test
    void unknownTemplateIsNotFound() throws Exception {
        getJson("/api/v1/workout-templates/999999")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEMPLATE_NOT_FOUND"));
    }
}
