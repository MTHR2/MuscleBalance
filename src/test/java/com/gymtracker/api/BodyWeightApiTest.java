package com.gymtracker.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BodyWeightApiTest extends ApiTestBase {

    private static final String B = "/api/v1/body-weights";

    @Test
    void savesTodayAndOverwritesSameDate() throws Exception {
        long id = idOf(postJson(B, "{\"weight\": 80.5}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value(today().toString()))
                .andExpect(jsonPath("$.weight").value(80.5)));

        postJson(B, "{\"date\": \"%s\", \"weight\": 81}".formatted(today()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.weight").value(81.0));
    }

    @Test
    void chartShowsPointsAndCurrentWeight() throws Exception {
        postJson(B, "{\"date\": \"%s\", \"weight\": 82}".formatted(today().minusDays(1)))
                .andExpect(status().isCreated());
        postJson(B, "{\"weight\": 80}").andExpect(status().isCreated());

        getJson(B + "?period=MONTH")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points", hasSize(2)))
                .andExpect(jsonPath("$.points[0].date").value(today().minusDays(1).toString()))
                .andExpect(jsonPath("$.points[1].avg7").value(81.0))
                .andExpect(jsonPath("$.current").value(80.0))
                .andExpect(jsonPath("$.target").value(nullValue()));
    }

    @Test
    void emptyChart() throws Exception {
        getJson(B)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points", hasSize(0)))
                .andExpect(jsonPath("$.current").value(nullValue()));
    }

    @Test
    void validatesMeasurement() throws Exception {
        postJson(B, "{\"date\": \"%s\", \"weight\": 80}".formatted(today().plusDays(1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("date"));
        postJson(B, "{\"weight\": 10}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("weight"));
        postJson(B, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("weight"));
    }

    @Test
    void deletesMeasurement() throws Exception {
        long id = idOf(postJson(B, "{\"weight\": 80}"));

        deleteJson(B + "/" + id).andExpect(status().isNoContent());

        deleteJson(B + "/" + id)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BODY_WEIGHT_NOT_FOUND"));
        getJson(B).andExpect(jsonPath("$.points", hasSize(0)));
    }

    @Test
    void profileTargetFeedsChart() throws Exception {
        getJson("/api/v1/profile")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetBodyWeight").value(nullValue()));

        putJson("/api/v1/profile", "{\"targetBodyWeight\": 75}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetBodyWeight").value(75.0));
        postJson(B, "{\"weight\": 80}");

        getJson(B)
                .andExpect(jsonPath("$.target").value(75.0))
                .andExpect(jsonPath("$.toTarget").value(-5.0));

        putJson("/api/v1/profile", "{\"targetBodyWeight\": null}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetBodyWeight").value(nullValue()));
        getJson(B).andExpect(jsonPath("$.toTarget").value(nullValue()));
    }

    @Test
    void validatesProfileTarget() throws Exception {
        putJson("/api/v1/profile", "{\"targetBodyWeight\": 10}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("targetBodyWeight"));

        getJson("/api/v1/profile").andExpect(jsonPath("$.targetBodyWeight").value(nullValue()));
    }
}
