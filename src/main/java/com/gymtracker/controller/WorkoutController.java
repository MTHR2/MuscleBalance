package com.gymtracker.controller;

import com.gymtracker.dto.workout.*;
import com.gymtracker.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutDetailsDto startWorkout(@Valid @RequestBody StartWorkoutRequestDto request) {
        return workoutService.startWorkout(request);
    }

    @GetMapping("/active")
    public WorkoutDetailsDto getActiveWorkout() {
        return workoutService.getActiveWorkout();

    }

    @GetMapping("/{id}")
    public WorkoutDetailsDto getWorkoutById(@PathVariable Long id) {
        return workoutService.getWorkout(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelWorkout(@PathVariable Long id) {
        workoutService.cancelWorkout(id);
    }

    @PostMapping("/{id}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutExerciseDto addExercise(@PathVariable Long id, @Valid @RequestBody AddWorkoutExerciseRequestDto request) {
        return workoutService.addExercise(id, request);
    }

    @PatchMapping("/{id}/exercises/{workoutExerciseId}")
    public WorkoutExerciseDto updateTargetSets(@PathVariable Long id,
                                               @PathVariable Long workoutExerciseId,
                                               @Valid @RequestBody UpdateTargetSetsRequestDto request) {
        return workoutService.updateTargetSets(id, workoutExerciseId, request);
    }

    @PostMapping("/{id}/exercises/{workoutExerciseId}/sets")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutSetDto addSet(@PathVariable Long id,
                                @PathVariable Long workoutExerciseId,
                                @Valid @RequestBody AddSetRequestDto request) {
        return workoutService.addSet(id, workoutExerciseId, request);
    }

    @PatchMapping("/{id}/exercises/{workoutExerciseId}/sets/{setId}")
    public WorkoutSetDto updateSet(@PathVariable Long id,
                                   @PathVariable Long workoutExerciseId,
                                   @PathVariable Long setId,
                                   @Valid @RequestBody UpdateSetRequestDto request) {
        return workoutService.updateSet(id, workoutExerciseId, setId, request);
    }
    @DeleteMapping("/{id}/exercises/{workoutExerciseId}/sets/{setId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSet(
            @PathVariable Long id,
            @PathVariable Long workoutExerciseId,
            @PathVariable Long setId) {

        workoutService.deleteSet(id,workoutExerciseId,setId);
    }

    @PostMapping("/{id}/finish")
    public WorkoutSummaryDto finishWorkout(@PathVariable Long id){
        return workoutService.finishWorkout(id);
    }



}
