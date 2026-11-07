package com.musclebalance.controller;

import com.musclebalance.dto.workout.*;
import com.musclebalance.exception.BusinessRuleException;
import com.musclebalance.exception.DomainErrorCode;
import com.musclebalance.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // история завершённых тренировок; finished=false не поддерживается (идущая тренировка: GET /active)
    @GetMapping
    public List<WorkoutSummaryDto> getFinishedWorkouts(@RequestParam(defaultValue = "true") boolean finished,
                                                       @RequestParam(defaultValue = "50") int limit) {
        if (!finished) {
            throw new BusinessRuleException(DomainErrorCode.WORKOUT_FILTER_UNSUPPORTED);
        }
        return workoutService.getFinishedWorkouts(Math.min(Math.max(limit, 1), 200));
    }

    @GetMapping("/active")
    public ResponseEntity<WorkoutDetailsDto> getActiveWorkout() {
        return workoutService.getActiveWorkout()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
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

    @DeleteMapping("/{id}/exercises/{workoutExerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable Long id, @PathVariable Long workoutExerciseId) {
        workoutService.deleteExercise(id, workoutExerciseId);
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
