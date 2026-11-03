package com.gymtracker.controller;

import com.gymtracker.dto.workouttemplate.CreateWorkoutTemplateRequestDto;
import com.gymtracker.dto.workouttemplate.WorkoutTemplateDetailsDto;
import com.gymtracker.dto.workouttemplate.WorkoutTemplateListItemDto;
import com.gymtracker.service.WorkoutTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workout-templates")
@RequiredArgsConstructor
public class WorkoutTemplateController {
    private final WorkoutTemplateService workoutTemplateService;

    @GetMapping
    public List<WorkoutTemplateListItemDto> getWorkoutList(){
        return workoutTemplateService.getWorkoutList();
    }

    @GetMapping("/{id}")
    public WorkoutTemplateDetailsDto getWorkoutTemplateDetailsById(@PathVariable Long id){
        return workoutTemplateService.getWorkoutTemplateDetailsById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutTemplateDetailsDto createWorkoutTemplate(@Valid @RequestBody CreateWorkoutTemplateRequestDto request){
        return workoutTemplateService.createWorkoutTemplate(request);
    }

}
