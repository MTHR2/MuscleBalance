package com.musclebalance.dto.exercise;

import jakarta.validation.constraints.NotNull;

public record FavouriteRequestDto(@NotNull Boolean favourite) {
}
