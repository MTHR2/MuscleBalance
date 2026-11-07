package com.musclebalance.controller;

import com.musclebalance.dto.exercise.CreateExerciseRequestDto;
import com.musclebalance.dto.exercise.ExerciseDetailsDto;
import com.musclebalance.dto.exercise.ExerciseListItemDto;
import com.musclebalance.dto.exercise.FavouriteRequestDto;
import com.musclebalance.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/exercises")
@RequiredArgsConstructor

public class ExerciseController {
    private final ExerciseService exerciseService;

    @GetMapping()
    public List<ExerciseListItemDto> getExercises(@RequestParam(required = false) String q,
                                                  @RequestParam(required = false) Short muscleGroupId,
                                                  @RequestParam(required = false) Boolean favourite,
                                                  @RequestParam(required = false) Boolean custom){
        return exerciseService.getExercises(q, muscleGroupId, favourite, custom);
    }

    @GetMapping("/{id}")
    public ExerciseDetailsDto getExerciseDetails(@PathVariable Long id){
        return exerciseService.getExerciseById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseDetailsDto createExercise(@Valid @RequestBody CreateExerciseRequestDto request){
        return exerciseService.createExercise(request);
    }

    @PutMapping("/{id}/favourite")
    public ExerciseDetailsDto setFavourite(@PathVariable Long id,
                                           @Valid @RequestBody FavouriteRequestDto request){
        return exerciseService.setFavourite(id, request.favourite());
    }

}
