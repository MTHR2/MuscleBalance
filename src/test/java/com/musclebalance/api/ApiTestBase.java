package com.musclebalance.api;

import com.musclebalance.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "DB_HOST=unused",
        "DB_PORT=5432",
        "DB_NAME=unused",
        "DB_USER=unused",
        "DB_PASSWORD=unused"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class ApiTestBase {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected Clock clock;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        jdbc.execute("DELETE FROM workouts");
        jdbc.execute("DELETE FROM plans");
        jdbc.execute("DELETE FROM workout_templates");
        jdbc.execute("DELETE FROM body_weights");
        jdbc.execute("DELETE FROM exercises WHERE is_custom");
        jdbc.execute("UPDATE exercises SET is_favourite = FALSE, working_weight = NULL");
        jdbc.execute("UPDATE profile SET target_body_weight = NULL");
    }

    protected ResultActions getJson(String url, Object... vars) throws Exception {
        return mvc.perform(get(url, vars).header("Accept-Language", "ru"));
    }

    protected ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "ru")
                .content(body));
    }

    protected ResultActions putJson(String url, String body) throws Exception {
        return mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "ru")
                .content(body));
    }

    protected ResultActions patchJson(String url, String body) throws Exception {
        return mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "ru")
                .content(body));
    }

    protected ResultActions deleteJson(String url) throws Exception {
        return mvc.perform(delete(url));
    }

    protected static String body(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    protected static <T> T read(ResultActions result, String path) throws Exception {
        return JsonPath.read(body(result), path);
    }

    protected static long idOf(ResultActions result) throws Exception {
        return ((Number) read(result, "$.id")).longValue();
    }

    protected LocalDate today() {
        return LocalDate.now(clock);
    }

    protected short todayDayOfWeek() {
        return (short) today().getDayOfWeek().getValue();
    }

    protected List<Map<String, Object>> catalog() throws Exception {
        return read(getJson("/api/v1/exercises").andExpect(status().isOk()), "$");
    }

    protected long seedExercise(String loadType, int index) throws Exception {
        List<Map<String, Object>> matching = catalog().stream()
                .filter(e -> loadType.equals(e.get("loadType")))
                .toList();
        return ((Number) matching.get(index).get("id")).longValue();
    }

    protected long weightedExercise(int index) throws Exception {
        return seedExercise("weighted", index);
    }

    protected List<Map<String, Object>> muscles() throws Exception {
        return read(getJson("/api/v1/muscles").andExpect(status().isOk()), "$");
    }

    protected short muscleId(int index) throws Exception {
        return ((Number) muscles().get(index).get("id")).shortValue();
    }

    protected long createTemplate(String name, long... exerciseIds) throws Exception {
        StringBuilder items = new StringBuilder();
        for (long id : exerciseIds) {
            if (!items.isEmpty()) {
                items.append(',');
            }
            items.append("{\"exerciseId\": %d, \"targetSets\": 3, \"repsMin\": 8, \"repsMax\": 12}".formatted(id));
        }
        return idOf(postJson("/api/v1/workout-templates",
                "{\"name\": \"%s\", \"exercises\": [%s]}".formatted(name, items))
                .andExpect(status().isCreated()));
    }

    protected long createPlan(String name, long templateId, int dayOfWeek) throws Exception {
        return idOf(postJson("/api/v1/plans", """
                {"name": "%s", "days": [{"workoutTemplateId": %d, "dayOfWeek": %d}]}
                """.formatted(name, templateId, dayOfWeek))
                .andExpect(status().isCreated()));
    }

    protected ResultActions startFromTemplate(long templateId) throws Exception {
        return postJson("/api/v1/workouts", "{\"workoutTemplateId\": %d}".formatted(templateId));
    }

    protected long firstWorkoutExerciseId(ResultActions workout) throws Exception {
        return ((Number) read(workout, "$.exercises[0].id")).longValue();
    }

    protected ResultActions addSet(long workoutId, long workoutExerciseId, int reps, double weight, boolean warmup)
            throws Exception {
        return postJson("/api/v1/workouts/%d/exercises/%d/sets".formatted(workoutId, workoutExerciseId),
                "{\"reps\": %d, \"weight\": %s, \"warmup\": %s}".formatted(reps, weight, warmup));
    }

    protected long finishedWorkoutFromTemplate(long templateId, int reps, double weight) throws Exception {
        ResultActions workout = startFromTemplate(templateId).andExpect(status().isCreated());
        long workoutId = idOf(workout);
        long workoutExerciseId = firstWorkoutExerciseId(workout);
        addSet(workoutId, workoutExerciseId, reps, weight, false).andExpect(status().isCreated());
        postJson("/api/v1/workouts/%d/finish".formatted(workoutId), "").andExpect(status().isOk());
        return workoutId;
    }
}
