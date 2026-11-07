package com.musclebalance.exception;

import com.musclebalance.controller.PlanController;
import com.musclebalance.controller.StatsController;
import com.musclebalance.controller.WorkoutController;
import com.musclebalance.service.PlanService;
import com.musclebalance.service.ProgressService;
import com.musclebalance.service.StatsService;
import com.musclebalance.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PlanController.class, WorkoutController.class, StatsController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlanService planService;

    @MockitoBean
    private WorkoutService workoutService;

    @MockitoBean
    private StatsService statsService;

    @MockitoBean
    private ProgressService progressService;

    private static final String VALID_PLAN = """
            {"name": "Сила", "days": [{"workoutTemplateId": 1, "dayOfWeek": 1}]}
            """;

    @Test
    void notFoundReturns404WithCode() throws Exception {
        when(planService.getPlan(anyLong())).thenThrow(NotFoundException.plan());

        mvc.perform(get("/api/v1/plans/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PLAN_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("План не найден"))
                .andExpect(jsonPath("$.path").value("/api/v1/plans/7"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void conflictReturns409WithFormattedMessage() throws Exception {
        when(planService.createPlan(any())).thenThrow(ConflictException.planNameTaken("Сила"));

        mvc.perform(post("/api/v1/plans").contentType(MediaType.APPLICATION_JSON).content(VALID_PLAN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PLAN_NAME_TAKEN"))
                .andExpect(jsonPath("$.message").value("План «Сила» уже есть"));
    }

    @Test
    void activeWorkoutConflictReturns409() throws Exception {
        when(workoutService.startWorkout(any())).thenThrow(ConflictException.workoutAlreadyActive());

        mvc.perform(post("/api/v1/workouts").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKOUT_ALREADY_ACTIVE"));
    }

    @Test
    void businessRuleReturns422() throws Exception {
        when(planService.createPlan(any()))
                .thenThrow(new BusinessRuleException(DomainErrorCode.PLAN_DAY_DUPLICATED));

        mvc.perform(post("/api/v1/plans").contentType(MediaType.APPLICATION_JSON).content(VALID_PLAN))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PLAN_DAY_DUPLICATED"));
    }

    @Test
    void invalidBodyReturns400WithFieldErrors() throws Exception {
        String body = """
                {"name": "", "days": [{"workoutTemplateId": 1, "dayOfWeek": 9}]}
                """;

        mvc.perform(post("/api/v1/plans").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("name")))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("days[0].dayOfWeek")));
    }

    @Test
    void invalidRequestParamReturns400WithFieldErrors() throws Exception {
        mvc.perform(get("/api/v1/stats/workouts/monthly").param("months", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("months"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/v1/plans").contentType(MediaType.APPLICATION_JSON).content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void pathTypeMismatchReturns400() throws Exception {
        mvc.perform(get("/api/v1/plans/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Неверное значение параметра «id»"));
    }

    @Test
    void unknownEnumParamReturns400() throws Exception {
        mvc.perform(get("/api/v1/stats/overview").param("period", "WEEK"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void unsupportedMethodReturns405() throws Exception {
        mvc.perform(put("/api/v1/workouts/active"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mvc.perform(get("/api/v1/nothing-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"));
    }

    @Test
    void unexpectedErrorReturns500WithoutDetails() throws Exception {
        when(planService.getPlanList()).thenThrow(new IllegalStateException("secret internal detail"));

        mvc.perform(get("/api/v1/plans"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(not(containsString("secret internal detail"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    @Test
    void missingActiveWorkoutReturns204() throws Exception {
        when(workoutService.getActiveWorkout()).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/workouts/active"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void unsupportedHistoryFilterReturns422() throws Exception {
        mvc.perform(get("/api/v1/workouts").param("finished", "false"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("WORKOUT_FILTER_UNSUPPORTED"));
    }
}
