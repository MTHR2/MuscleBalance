package com.gymtracker.mapper;

import com.gymtracker.dto.muscle.MuscleItemDto;
import com.gymtracker.entity.Muscle;
import org.springframework.stereotype.Component;

@Component
public class MuscleMapper {
    public MuscleItemDto toMuscleItemDto(Muscle muscle) {
        return new MuscleItemDto(muscle.getId(), muscle.getName(), muscle.getRegionKey(), muscle.getImageUrl(),
                muscle.getMuscleGroup().getId(), muscle.getMuscleGroup().getName());
    }
}
