package com.gymtracker.dto.muscle;

public record MuscleItemDto(Short id,
                            String name,
                            String regionKey,
                            String imageUrl,
                            Short groupId,
                            String groupName) {
}
