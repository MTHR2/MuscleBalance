package com.gymtracker.controller;

import com.gymtracker.dto.ExerciseDetailsDto;
import com.gymtracker.dto.ExerciseListItemDto;
import com.gymtracker.dto.ExerciseMuscleDto;
import com.gymtracker.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/exercises")
@RequiredArgsConstructor

public class ExerciseController {
    private final ExerciseService exerciseService;

    @GetMapping()
    public List<ExerciseListItemDto> getExercisesByMuscleGroup (@RequestParam Short muscleGroupId){
        return exerciseService.getExercisesForMuscleGroup(muscleGroupId);
    }
    @GetMapping("/{id}")
    public ExerciseDetailsDto getExerciseDetails(@PathVariable Long id){
        return exerciseService.getExerciseById(id);
    }


}
