package com.gymtracker.dto.exercise;

import jakarta.validation.constraints.NotNull;

public record FavouriteRequestDto(@NotNull Boolean favourite) {
}
