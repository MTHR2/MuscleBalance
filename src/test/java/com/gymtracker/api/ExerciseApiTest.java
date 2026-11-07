package com.gymtracker.api;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExerciseApiTest extends ApiTestBase {

    private String customExercise(String name, short primary, short secondary) {
        return """
                {"name": "%s", "description": "Своё упражнение", "loadType": "weighted",
                 "muscles": [{"muscleId": %d, "role": "primary"}, {"muscleId": %d, "role": "secondary"}]}
                """.formatted(name, primary, secondary);
    }

    @Test
    void listsSeededCatalog() throws Exception {
        getJson("/api/v1/exercises")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[*].custom", everyItem(is(false))))
                .andExpect(jsonPath("$[*].favourite", everyItem(is(false))));
    }

    @Test
    void searchesByName() throws Exception {
        String name = (String) catalog().getFirst().get("name");
        String query = name.split(" ")[0].toLowerCase();

        getJson("/api/v1/exercises?q={q}", query)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[*].name", everyItem(containsStringIgnoringCase(query))));
    }

    @Test
    void createsCustomExerciseAndFindsItByFilters() throws Exception {
        short primary = muscleId(0);
        short secondary = muscleId(1);
        Map<String, Object> primaryMuscle = muscles().getFirst();

        long id = idOf(postJson("/api/v1/exercises", customExercise("Тяга резинки", primary, secondary))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Тяга резинки"))
                .andExpect(jsonPath("$.loadType").value("weighted"))
                .andExpect(jsonPath("$.custom").value(true))
                .andExpect(jsonPath("$.favourite").value(false))
                .andExpect(jsonPath("$.muscles", hasSize(2))));

        getJson("/api/v1/exercises/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Своё упражнение"))
                .andExpect(jsonPath("$.muscles[?(@.role == 'primary')].muscleId").value(hasItem((int) primary)));

        getJson("/api/v1/exercises?custom=true")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        getJson("/api/v1/exercises?custom=true&muscleGroupId={g}", primaryMuscle.get("groupId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem((int) id)));
    }

    @Test
    void rejectsDuplicateNameIgnoringCase() throws Exception {
        postJson("/api/v1/exercises", customExercise("Тяга резинки", muscleId(0), muscleId(1)))
                .andExpect(status().isCreated());

        postJson("/api/v1/exercises", customExercise("ТЯГА РЕЗИНКИ", muscleId(0), muscleId(1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCISE_NAME_TAKEN"));
    }

    @Test
    void rejectsSeededName() throws Exception {
        String seeded = (String) catalog().getFirst().get("name");

        postJson("/api/v1/exercises", customExercise(seeded, muscleId(0), muscleId(1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCISE_NAME_TAKEN"));
    }

    @Test
    void validatesRequestBody() throws Exception {
        postJson("/api/v1/exercises", """
                {"name": " ", "loadType": "weighted", "muscles": []}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("muscles")));
    }

    @Test
    void rejectsUnknownLoadType() throws Exception {
        postJson("/api/v1/exercises", """
                {"name": "Жим", "loadType": "machine", "muscles": [{"muscleId": 1, "role": "primary"}]}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void requiresPrimaryMuscle() throws Exception {
        postJson("/api/v1/exercises", """
                {"name": "Жим", "loadType": "weighted", "muscles": [{"muscleId": %d, "role": "secondary"}]}
                """.formatted(muscleId(0)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("EXERCISE_PRIMARY_MUSCLE_REQUIRED"));
    }

    @Test
    void rejectsDuplicatedMuscle() throws Exception {
        postJson("/api/v1/exercises", customExercise("Жим", muscleId(0), muscleId(0)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("EXERCISE_MUSCLE_DUPLICATED"));
    }

    @Test
    void rejectsUnknownMuscle() throws Exception {
        postJson("/api/v1/exercises", customExercise("Жим", muscleId(0), (short) 9999))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("MUSCLES_NOT_FOUND"));
    }

    @Test
    void togglesFavourite() throws Exception {
        long id = weightedExercise(0);

        putJson("/api/v1/exercises/%d/favourite".formatted(id), "{\"favourite\": true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favourite").value(true));

        getJson("/api/v1/exercises?favourite=true")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        putJson("/api/v1/exercises/%d/favourite".formatted(id), "{\"favourite\": false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favourite").value(false));

        getJson("/api/v1/exercises?favourite=true")
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void favouriteRequiresValue() throws Exception {
        putJson("/api/v1/exercises/%d/favourite".formatted(weightedExercise(0)), "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("favourite"));
    }

    @Test
    void unknownExerciseIsNotFound() throws Exception {
        getJson("/api/v1/exercises/999999")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCISE_NOT_FOUND"));

        putJson("/api/v1/exercises/999999/favourite", "{\"favourite\": true}")
                .andExpect(status().isNotFound());
    }
}
