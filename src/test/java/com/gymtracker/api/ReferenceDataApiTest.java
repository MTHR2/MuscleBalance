package com.gymtracker.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReferenceDataApiTest extends ApiTestBase {

    @Test
    void listsMuscleGroups() throws Exception {
        getJson("/api/v1/muscle-groups")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[*].id", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].name", everyItem(notNullValue())));
    }

    @Test
    void returnsMuscleGroupById() throws Exception {
        List<Map<String, Object>> groups = read(getJson("/api/v1/muscle-groups"), "$");
        Map<String, Object> first = groups.getFirst();

        getJson("/api/v1/muscle-groups/{id}", first.get("id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(first.get("name")));
    }

    @Test
    void unknownMuscleGroupIsNotFound() throws Exception {
        getJson("/api/v1/muscle-groups/999")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MUSCLE_GROUP_NOT_FOUND"));
    }

    @Test
    void listsMusclesWithTheirGroups() throws Exception {
        getJson("/api/v1/muscles")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[*].groupId", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].groupName", everyItem(notNullValue())));
    }
}
