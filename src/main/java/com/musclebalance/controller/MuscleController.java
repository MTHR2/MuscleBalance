package com.musclebalance.controller;

import com.musclebalance.dto.muscle.MuscleItemDto;
import com.musclebalance.service.MuscleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/muscles")
@RequiredArgsConstructor
public class MuscleController {
    private final MuscleService muscleService;

    @GetMapping
    public List<MuscleItemDto> getAllMuscles() {
        return muscleService.getAllMuscles();
    }
}
