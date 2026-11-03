package com.gymtracker.controller;

import com.gymtracker.dto.plan.PlanDetailsDto;
import com.gymtracker.dto.plan.PlanListItemDto;
import com.gymtracker.dto.plan.SavePlanRequestDto;
import com.gymtracker.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;


    @GetMapping
    public List<PlanListItemDto> getListOfPlans(){
        return planService.getPlanList();
    }

    @GetMapping("/{id}")
    public PlanDetailsDto getPlanDetailsById(@PathVariable Long id){
        return planService.getPlan(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanDetailsDto createPlan(@Valid @RequestBody SavePlanRequestDto request){
        return planService.createPlan(request);
    }

    @PatchMapping("/{id}/activate")
    public PlanDetailsDto activatePlan(@PathVariable Long id){
        return planService.activatePlan(id);
    }












    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlanById (@PathVariable Long id){
        planService.deletePlan(id);
    }

}
