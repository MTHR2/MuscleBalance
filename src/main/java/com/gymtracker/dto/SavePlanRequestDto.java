package com.gymtracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavePlanRequestDto(@NotBlank String name,
                                  String description,
                                  @Valid @NotEmpty @Size(max = 7) List<PlanDayRequestDto> days) {
}
