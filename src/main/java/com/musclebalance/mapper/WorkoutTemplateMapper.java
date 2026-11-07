package com.musclebalance.mapper;

import com.musclebalance.dto.workouttemplate.*;
import com.musclebalance.entity.Exercise;
import com.musclebalance.entity.WorkoutTemplate;
import com.musclebalance.entity.WorkoutTemplateExercise;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WorkoutTemplateMapper {
    private WorkoutTemplateExerciseDto toExerciseDto(
            WorkoutTemplateExercise workoutTemplateExercise){
        return new WorkoutTemplateExerciseDto(
                workoutTemplateExercise.getExercise().getId(), workoutTemplateExercise.getExercise().getName(),
                workoutTemplateExercise.getExercise().getImageUrl(),
                workoutTemplateExercise.getSortOrder(), workoutTemplateExercise.getTargetSets(),
                workoutTemplateExercise.getRepsMin(), workoutTemplateExercise.getRepsMax());
    }

    public WorkoutTemplateDetailsDto toDetailsDto(WorkoutTemplate workoutTemplate){
        return new WorkoutTemplateDetailsDto(
                workoutTemplate.getId(),
                workoutTemplate.getName(),
                workoutTemplate.getExercises().stream().map(this::toExerciseDto).toList()
        );
    }
    public WorkoutTemplateListItemDto toListItemDto(WorkoutTemplate workoutTemplate){
        return new WorkoutTemplateListItemDto(workoutTemplate.getId(),
                workoutTemplate.getName(), workoutTemplate.getExercises().size());
    }

    public WorkoutTemplate toEntity(CreateWorkoutTemplateRequestDto requestDto, Map<Long, Exercise> exerciseMap){
        var workoutTemplate = new WorkoutTemplate();
        workoutTemplate.setName(requestDto.name());

        for (int i = 0; i < requestDto.exercises().size(); i++) {
            var exercise = new WorkoutTemplateExercise();
            WorkoutTemplateExerciseRequestDto item = requestDto.exercises().get(i);
            exercise.setExercise(exerciseMap.get(item.exerciseId()));
            exercise.setSortOrder((short) i);
            exercise.setTargetSets(item.targetSets());
            exercise.setRepsMin(item.repsMin());
            exercise.setRepsMax(item.repsMax());
            exercise.setWorkoutTemplate(workoutTemplate);
            workoutTemplate.getExercises().add(exercise);
        }

        return workoutTemplate;
    }
}
