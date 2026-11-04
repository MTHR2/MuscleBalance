package com.gymtracker.mapper;

import com.gymtracker.dto.plan.*;
import com.gymtracker.dto.workouttemplate.WorkoutTemplateDetailsDto;
import com.gymtracker.entity.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PlanMapper {

    private PlanDayDto toPlanDto(PlanDay planDay) {
        return new PlanDayDto(planDay.getDayOfWeek(),
                planDay.getWorkoutTemplate().getId(),planDay.getWorkoutTemplate().getName());
    }

    public PlanDetailsDto toDetailsDto(Plan plan){
        return new PlanDetailsDto(plan.getId(),plan.getName(), plan.getDescription(),
                plan.isActive(), plan.getDays().stream().map(this::toPlanDto).toList(),
                plan.getDefaultWeeklySets(), plan.getMuscleTargets().stream().map(this::toMuscleTargetDto).toList());
    }

    private MuscleTargetDto toMuscleTargetDto(PlanMuscleTarget planMuscleTarget) {
        return new MuscleTargetDto(planMuscleTarget.getMuscle().getId(),planMuscleTarget.getMuscle().getName(),
                planMuscleTarget.getWeeklySets());
    }

    public PlanListItemDto toListItemDto(Plan plan){
        return new PlanListItemDto(plan.getId(), plan.getName(),
                plan.isActive(),plan.getDays().size());

    }


    public Plan toEntity (SavePlanRequestDto request, Map<Long, WorkoutTemplate> map, Map<Short, Muscle> muscles ){
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
        plan.setDefaultWeeklySets(request.defaultWeeklySets());
        if (request.muscleTargets() != null) {
            for (MuscleTargetRequestDto item : request.muscleTargets()) {
                var target = new PlanMuscleTarget();
                target.setPlan(plan);
                target.setMuscle(muscles.get(item.muscleId()));
                target.setWeeklySets(item.weeklySets());
                plan.getMuscleTargets().add(target);
            }
        }
        return plan;

    }

    public TodayWorkoutDto toTodayDto(WorkoutTemplateDetailsDto template, boolean completedToday) {
        return new TodayWorkoutDto(template.id(), template.name(), template.exercises(), completedToday);
    }

    /**
     * Переносит запрос в существующий план. Дни и цели правим на месте (по дню недели и по мышце),
     * а не пересоздаём: иначе Hibernate вставит новую строку раньше удаления старой
     * и упрётся в уникальные ключи (plan_id, day_of_week) и (plan_id, muscle_id).
     */
    public void updateEntity(Plan plan, SavePlanRequestDto request,
                             Map<Long, WorkoutTemplate> templates, Map<Short, Muscle> muscles) {
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setDefaultWeeklySets(request.defaultWeeklySets());

        Set<Short> requestedDays = new HashSet<>();
        for (PlanDayRequestDto item : request.days()) {
            requestedDays.add(item.dayOfWeek());
        }
        plan.getDays().removeIf(day -> !requestedDays.contains(day.getDayOfWeek()));
        for (PlanDayRequestDto item : request.days()) {
            PlanDay day = plan.getDays().stream()
                    .filter(d -> d.getDayOfWeek().equals(item.dayOfWeek()))
                    .findFirst()
                    .orElseGet(() -> {
                        var created = new PlanDay();
                        created.setPlan(plan);
                        created.setDayOfWeek(item.dayOfWeek());
                        plan.getDays().add(created);
                        return created;
                    });
            day.setWorkoutTemplate(templates.get(item.workoutTemplateId()));
        }

        List<MuscleTargetRequestDto> requestedTargets =
                request.muscleTargets() != null ? request.muscleTargets() : List.of();
        Set<Short> requestedMuscles = new HashSet<>();
        for (MuscleTargetRequestDto item : requestedTargets) {
            requestedMuscles.add(item.muscleId());
        }
        plan.getMuscleTargets().removeIf(t -> !requestedMuscles.contains(t.getMuscle().getId()));
        for (MuscleTargetRequestDto item : requestedTargets) {
            PlanMuscleTarget target = plan.getMuscleTargets().stream()
                    .filter(t -> t.getMuscle().getId().equals(item.muscleId()))
                    .findFirst()
                    .orElseGet(() -> {
                        var created = new PlanMuscleTarget();
                        created.setPlan(plan);
                        created.setMuscle(muscles.get(item.muscleId()));
                        plan.getMuscleTargets().add(created);
                        return created;
                    });
            target.setWeeklySets(item.weeklySets());
        }
    }

}
