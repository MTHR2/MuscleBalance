package com.musclebalance.controller;

import com.musclebalance.dto.muscle.MuscleGroupCardDto;

import com.musclebalance.service.MuscleGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/muscle-groups")
@RequiredArgsConstructor

public class MuscleGroupController {
    private final MuscleGroupService muscleGroupService;
    @GetMapping()
    public List<MuscleGroupCardDto> getAllMuscleGroupCards(){
        return muscleGroupService.getAllMuscleGroups();
    }

    @GetMapping("/{id}")
    public MuscleGroupCardDto getMuscleGroupById(@PathVariable Short id){
        return muscleGroupService.getMuscleGroupById(id);
    }
}
