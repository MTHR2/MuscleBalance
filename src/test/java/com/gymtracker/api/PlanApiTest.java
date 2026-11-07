package com.gymtracker.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlanApiTest extends ApiTestBase {

    @Test
    void firstPlanBecomesActive() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));

        postJson("/api/v1/plans", """
                {"name": "Сила", "description": "3 раза в неделю",
                 "days": [{"workoutTemplateId": %d, "dayOfWeek": 1}, {"workoutTemplateId": %d, "dayOfWeek": 3}],
                 "defaultWeeklySets": 10}
                """.formatted(template, template))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.description").value("3 раза в неделю"))
                .andExpect(jsonPath("$.defaultWeeklySets").value(10))
                .andExpect(jsonPath("$.days", hasSize(2)))
                .andExpect(jsonPath("$.days[0].workoutTemplateName").value("Верх"));

        long second = createPlan("Объём", template, 2);
        getJson("/api/v1/plans/{id}", second)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void listsActivePlanFirst() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        createPlan("Яблоко", template, 1);
        long active = createPlan("Апельсин", template, 2);
        patchJson("/api/v1/plans/%d/activate".formatted(active), "").andExpect(status().isOk());

        getJson("/api/v1/plans")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Апельсин"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].workoutCount").value(1))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    @Test
    void activationSwitchesActivePlan() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        long first = createPlan("Первый", template, 1);
        long second = createPlan("Второй", template, 2);

        patchJson("/api/v1/plans/%d/activate".formatted(second), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        getJson("/api/v1/plans/{id}", first).andExpect(jsonPath("$.active").value(false));

        patchJson("/api/v1/plans/%d/activate".formatted(second), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updatesPlan() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        long plan = createPlan("Сила", template, 1);
        short muscle = muscleId(0);

        putJson("/api/v1/plans/" + plan, """
                {"name": "Сила 2", "days": [{"workoutTemplateId": %d, "dayOfWeek": 2},
                                           {"workoutTemplateId": %d, "dayOfWeek": 4}],
                 "defaultWeeklySets": 12, "muscleTargets": [{"muscleId": %d, "weeklySets": 16}]}
                """.formatted(template, template, muscle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Сила 2"))
                .andExpect(jsonPath("$.days[*].dayOfWeek").value(hasItem(4)))
                .andExpect(jsonPath("$.days", hasSize(2)))
                .andExpect(jsonPath("$.defaultWeeklySets").value(12))
                .andExpect(jsonPath("$.muscleTargets[0].muscleId").value((int) muscle))
                .andExpect(jsonPath("$.muscleTargets[0].weeklySets").value(16));
    }

    @Test
    void updateAllowsChangingCaseOfOwnName() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        long plan = createPlan("Сила", template, 1);

        putJson("/api/v1/plans/" + plan, """
                {"name": "СИЛА", "days": [{"workoutTemplateId": %d, "dayOfWeek": 1}]}
                """.formatted(template))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("СИЛА"));
    }

    @Test
    void rejectsNameTakenByAnotherPlan() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        createPlan("Сила", template, 1);
        long other = createPlan("Объём", template, 2);

        postJson("/api/v1/plans", """
                {"name": "сила", "days": [{"workoutTemplateId": %d, "dayOfWeek": 1}]}
                """.formatted(template))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLAN_NAME_TAKEN"));

        putJson("/api/v1/plans/" + other, """
                {"name": "Сила", "days": [{"workoutTemplateId": %d, "dayOfWeek": 1}]}
                """.formatted(template))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLAN_NAME_TAKEN"));
    }

    @Test
    void rejectsBrokenDaysAndTargets() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        short muscle = muscleId(0);

        postJson("/api/v1/plans", """
                {"name": "П", "days": [{"workoutTemplateId": %d, "dayOfWeek": 1},
                                      {"workoutTemplateId": %d, "dayOfWeek": 1}]}
                """.formatted(template, template))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PLAN_DAY_DUPLICATED"));

        postJson("/api/v1/plans", """
                {"name": "П", "days": [{"workoutTemplateId": 999999, "dayOfWeek": 1}]}
                """)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("TEMPLATES_NOT_FOUND"));

        postJson("/api/v1/plans", """
                {"name": "П", "days": [{"workoutTemplateId": %d, "dayOfWeek": 1}],
                 "muscleTargets": [{"muscleId": %d, "weeklySets": 10}, {"muscleId": %d, "weeklySets": 12}]}
                """.formatted(template, muscle, muscle))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PLAN_MUSCLE_TARGET_DUPLICATED"));

        postJson("/api/v1/plans", """
                {"name": "П", "days": [{"workoutTemplateId": %d, "dayOfWeek": 1}],
                 "muscleTargets": [{"muscleId": 9999, "weeklySets": 10}]}
                """.formatted(template))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("MUSCLES_NOT_FOUND"));

        postJson("/api/v1/plans", """
                {"name": "П", "days": [{"workoutTemplateId": %d, "dayOfWeek": 8}]}
                """.formatted(template))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("days[0].dayOfWeek"));

        getJson("/api/v1/plans").andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deletesPlan() throws Exception {
        long plan = createPlan("Сила", createTemplate("Верх", weightedExercise(0)), 1);

        deleteJson("/api/v1/plans/" + plan).andExpect(status().isNoContent());

        getJson("/api/v1/plans/{id}", plan)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAN_NOT_FOUND"));
        deleteJson("/api/v1/plans/" + plan).andExpect(status().isNotFound());
    }

    @Test
    void unknownPlanIsNotFound() throws Exception {
        getJson("/api/v1/plans/999999").andExpect(status().isNotFound());
        patchJson("/api/v1/plans/999999/activate", "").andExpect(status().isNotFound());
        putJson("/api/v1/plans/999999", """
                {"name": "П", "days": [{"workoutTemplateId": 1, "dayOfWeek": 1}]}
                """).andExpect(status().isNotFound());
    }

    @Test
    void todayWorkoutIsEmptyWithoutPlan() throws Exception {
        getJson("/api/v1/plans/active/today")
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void todayWorkoutIsEmptyOnRestDay() throws Exception {
        int restDay = todayDayOfWeek() % 7 + 1;
        createPlan("Сила", createTemplate("Верх", weightedExercise(0)), restDay);

        getJson("/api/v1/plans/active/today").andExpect(status().isNoContent());
    }

    @Test
    void todayWorkoutComesFromActivePlanAndTracksCompletion() throws Exception {
        long template = createTemplate("Верх", weightedExercise(0));
        createPlan("Сила", template, todayDayOfWeek());

        getJson("/api/v1/plans/active/today")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(template))
                .andExpect(jsonPath("$.name").value("Верх"))
                .andExpect(jsonPath("$.exercises", hasSize(1)))
                .andExpect(jsonPath("$.completedToday").value(false));

        finishedWorkoutFromTemplate(template, 10, 50);

        getJson("/api/v1/plans/active/today")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedToday").value(true));
    }
}
