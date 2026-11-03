package com.gymtracker.mapper;

import com.gymtracker.dto.muscle.MuscleGroupCardDto;
import com.gymtracker.entity.MuscleGroup;
import org.springframework.stereotype.Component;

@Component
public class MuscleGroupMapper {
    public MuscleGroupCardDto toMuscleGroupCardDto(MuscleGroup muscleGroup){
        return new MuscleGroupCardDto(muscleGroup.getId(),
                                    muscleGroup.getName(),
                                    muscleGroup.getImageUrl());
    }
}
