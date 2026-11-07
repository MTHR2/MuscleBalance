package com.musclebalance.mapper;

import com.musclebalance.dto.muscle.MuscleGroupCardDto;
import com.musclebalance.entity.MuscleGroup;
import org.springframework.stereotype.Component;

@Component
public class MuscleGroupMapper {
    public MuscleGroupCardDto toMuscleGroupCardDto(MuscleGroup muscleGroup){
        return new MuscleGroupCardDto(muscleGroup.getId(),
                                    muscleGroup.getName(),
                                    muscleGroup.getImageUrl());
    }
}
