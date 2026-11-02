package com.gymtracker.mapper;

import com.gymtracker.dto.*;
import com.gymtracker.entity.Plan;
import com.gymtracker.entity.PlanDay;
import com.gymtracker.entity.WorkoutTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PlanMapper {

    private PlanDayDto toPlanDto(PlanDay planDay) {
        return new PlanDayDto(planDay.getDayOfWeek(),
                planDay.getWorkoutTemplate().getId(),planDay.getWorkoutTemplate().getName());
    }

    public PlanDetailsDto toDetailsDto(Plan plan){
        return new PlanDetailsDto(plan.getId(),plan.getName(),plan.getDescription(),
                plan.isActive(), plan.getDays().stream().map(this::toPlanDto).toList());
    }

    public PlanListItemDto toListItemDto(Plan plan){
        return new PlanListItemDto(plan.getId(), plan.getName(),
                plan.isActive(),plan.getDays().size());

    }


    public Plan toEntity (SavePlanRequestDto request, Map<Long, WorkoutTemplate> map){
        var plan = new Plan();
        plan.setName(request.name());
        plan.setDescription(request.description());

        for (int i = 0; i < request.days().size(); i++) {
            var planDay = new PlanDay();
            PlanDayRequestDto item = request.days().get(i);
            planDay.setPlan(plan);
            planDay.setWorkoutTemplate(map.get(item.workoutTemplateId()));
            planDay.setDayOfWeek(item.dayOfWeek());
            plan.getDays().add(planDay);
        }

        return plan;

    }

}
