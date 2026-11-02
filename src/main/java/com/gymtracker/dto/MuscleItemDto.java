package com.gymtracker.dto;

public record MuscleItemDto(Long id,
                            String name,
                            String imageUrl,
                            Short groupId,
                            String groupName) {
}
