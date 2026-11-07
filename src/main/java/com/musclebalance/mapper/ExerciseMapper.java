package com.musclebalance.mapper;


import com.musclebalance.dto.exercise.CreateExerciseRequestDto;
import com.musclebalance.dto.exercise.ExerciseDetailsDto;
import com.musclebalance.dto.exercise.ExerciseListItemDto;
import com.musclebalance.dto.exercise.ExerciseMuscleDto;
import com.musclebalance.dto.exercise.ExerciseMuscleRequestDto;
import com.musclebalance.entity.Exercise;
import com.musclebalance.entity.ExerciseMuscle;
import com.musclebalance.entity.Muscle;
import org.springframework.stereotype.Component;

import java.util.Map;

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
                exercise.isFavourite(), exercise.isCustom(), exercise.getWorkingWeight(),
                exercise.getMuscles().stream().map(this::toExerciseMuscleDto).toList());

    }

    // своё упражнение: custom = true, без картинки, не в избранном
    public Exercise toEntity(CreateExerciseRequestDto request, Map<Short, Muscle> muscles) {
        var exercise = new Exercise();
        exercise.setName(request.name().trim());
        exercise.setDescription(request.description());
        exercise.setLoadType(request.loadType());
        exercise.setCustom(true);

        for (ExerciseMuscleRequestDto item : request.muscles()) {
            var exerciseMuscle = new ExerciseMuscle();
            exerciseMuscle.setExercise(exercise);
            exerciseMuscle.setMuscle(muscles.get(item.muscleId()));
            exerciseMuscle.setMuscleRole(item.role());
            exercise.getMuscles().add(exerciseMuscle);
        }
        return exercise;
    }

}
