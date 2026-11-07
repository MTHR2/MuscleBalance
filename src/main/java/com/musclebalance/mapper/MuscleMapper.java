package com.musclebalance.mapper;

import com.musclebalance.dto.muscle.MuscleItemDto;
import com.musclebalance.entity.Muscle;
import org.springframework.stereotype.Component;

@Component
public class MuscleMapper {
    public MuscleItemDto toMuscleItemDto(Muscle muscle) {
        return new MuscleItemDto(muscle.getId(), muscle.getName(), muscle.getRegionKey(), muscle.getImageUrl(),
                muscle.getMuscleGroup().getId(), muscle.getMuscleGroup().getName());
    }
}
