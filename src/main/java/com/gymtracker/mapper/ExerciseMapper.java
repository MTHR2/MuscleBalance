package com.gymtracker.mapper;


import com.gymtracker.dto.exercise.ExerciseDetailsDto;
import com.gymtracker.dto.exercise.ExerciseListItemDto;
import com.gymtracker.dto.exercise.ExerciseMuscleDto;
import com.gymtracker.entity.Exercise;
import com.gymtracker.entity.ExerciseMuscle;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {
    private ExerciseMuscleDto toExerciseMuscleDto(ExerciseMuscle exerciseMuscle){
        return new ExerciseMuscleDto(exerciseMuscle.getMuscle().getId(),
                                    exerciseMuscle.getMuscle().getName(),
                                    exerciseMuscle.getMuscleRole());

    }
    public ExerciseDetailsDto toDetailsDto(Exercise exercise){
        return new ExerciseDetailsDto(exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getImageUrl(),
                exercise.getLoadType(),
                exercise.isCustom(),
                exercise.isFavourite(),
                exercise.getWorkingWeight(),
                exercise.getMuscles().stream().map(this::toExerciseMuscleDto).toList());
    }
    public ExerciseListItemDto toListItemDto (Exercise exercise){
        return new ExerciseListItemDto(exercise.getId(),
                exercise.getName(), exercise.getImageUrl(), exercise.getLoadType(),
                exercise.isFavourite(), exercise.isCustom(),
                exercise.getMuscles().stream().map(this::toExerciseMuscleDto).toList());

    }

}
