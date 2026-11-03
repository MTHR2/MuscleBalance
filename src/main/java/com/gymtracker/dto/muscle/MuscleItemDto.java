package com.gymtracker.dto.muscle;

public record MuscleItemDto(Short id,
                            String name,
                            String imageUrl,
                            Short groupId,
                            String groupName) {
}
