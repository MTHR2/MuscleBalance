package com.gymtracker.mapper;

import com.gymtracker.dto.workout.AddSetRequestDto;
import com.gymtracker.dto.workout.UpdateSetRequestDto;
import com.gymtracker.dto.workout.WorkoutDetailsDto;
import com.gymtracker.dto.workout.WorkoutExerciseDto;
import com.gymtracker.dto.workout.WorkoutSetDto;
import com.gymtracker.dto.workout.WorkoutSummaryDto;
import com.gymtracker.entity.*;
import org.springframework.stereotype.Component;

@Component
public class WorkoutMapper {
    public WorkoutSetDto toSetDto(WorkoutSet workoutSet) {
        return new WorkoutSetDto(workoutSet.getId(), workoutSet.getSetNumber(),
                workoutSet.getReps(), workoutSet.getWeight(), workoutSet.isWarmup());
    }

    public WorkoutDetailsDto toDetailsDto(Workout workout) {
        return new WorkoutDetailsDto(workout.getId(),
                workout.getName(), workout.getStartedAt(), workout.getFinishedAt(),
                workout.getExercises().stream().map(this::toExerciseDto).toList());
    }

    public WorkoutSummaryDto toSummaryDto(Workout workout, long durationSeconds,
                                          Integer completionPercent, int completedSets) {
        return new WorkoutSummaryDto(workout.getId(), workout.getName(), workout.getStartedAt(),
                workout.getFinishedAt(), durationSeconds, completionPercent, completedSets);
    }

    public WorkoutExerciseDto toExerciseDto(WorkoutExercise workExsc) {
        return new WorkoutExerciseDto(workExsc.getId(), workExsc.getExercise().getId(),
                workExsc.getExercise().getName(), workExsc.getExercise().getImageUrl(),
                workExsc.getExercise().getLoadType(), workExsc.getSortOrder(), workExsc.getPlannedSets(),
                workExsc.getTargetSets(), workExsc.getRepsMin(), workExsc.getRepsMax(), workExsc.getExercise().getWorkingWeight(),
                workExsc.getSets().stream().map(this::toSetDto).toList());
    }

    public Workout createFromTemplate(WorkoutTemplate template) {
        var workout = new Workout();
        workout.setName(template.getName());
        workout.setWorkoutTemplate(template);
        for (WorkoutTemplateExercise item : template.getExercises()) {
            var workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(item.getExercise());
            workoutExercise.setSortOrder(item.getSortOrder());
            workoutExercise.setPlannedSets(item.getTargetSets());
            workoutExercise.setTargetSets(item.getTargetSets());
            workoutExercise.setRepsMin(item.getRepsMin());
            workoutExercise.setRepsMax(item.getRepsMax());
            workout.getExercises().add(workoutExercise);
        }

        return workout;
    }

    public Workout createFree(String name) {
        var workout = new Workout();
        workout.setName(name);
        return workout;
    }

    public WorkoutExercise createExercise(Workout workout, Exercise exercise, Short targetSets, short sortOrder) {
        var workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setSortOrder(sortOrder);
        workoutExercise.setTargetSets(targetSets);
        return workoutExercise;
    }


    public WorkoutSet createSet(WorkoutExercise workoutExercise, AddSetRequestDto request, short setNumber) {
        var workoutSet = new WorkoutSet();
        workoutSet.setWorkoutExercise(workoutExercise);
        workoutSet.setSetNumber(setNumber);
        workoutSet.setReps(request.reps());
        workoutSet.setWeight(request.weight());
        workoutSet.setWarmup(Boolean.TRUE.equals(request.warmup()));
        return workoutSet;
    }

    public void updateSet(WorkoutSet workoutSet, UpdateSetRequestDto request) {
        workoutSet.setReps(request.reps());
        workoutSet.setWeight(request.weight());
        workoutSet.setWarmup(request.warmup());
    }
}
