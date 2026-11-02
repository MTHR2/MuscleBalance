package com.gymtracker.service;

import com.gymtracker.dto.PlanDayRequestDto;
import com.gymtracker.dto.PlanDetailsDto;
import com.gymtracker.dto.PlanListItemDto;
import com.gymtracker.dto.SavePlanRequestDto;
import com.gymtracker.entity.Plan;
import com.gymtracker.entity.WorkoutTemplate;
import com.gymtracker.mapper.PlanMapper;
import com.gymtracker.repository.PlanRepository;
import com.gymtracker.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanMapper planMapper;
    private final PlanRepository planRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;


    @Transactional(readOnly = true)
    public List<PlanListItemDto> getPlanList() {
        return planRepository.findAllByOrderByActiveDescNameAsc().
                stream().map(planMapper::toListItemDto).toList();
    }

    @Transactional(readOnly = true)
    public PlanDetailsDto getPlan(Long planId) {
        return planRepository.findWithDaysById(planId).map(planMapper::toDetailsDto)
                .orElseThrow(() -> new NoSuchElementException("something")); // наугад пихнул исключение
    }

    @Transactional
    public PlanDetailsDto activatePlan(Long id) {
        Plan planToActivate = planRepository.findWithDaysById(id).orElseThrow();
        if (planToActivate.isActive()) {
            return planMapper.toDetailsDto(planToActivate);
        }

        planRepository.findByActiveTrue().ifPresent(p -> p.setActive(false));
        planRepository.flush();
        planToActivate.setActive(true);
        return planMapper.toDetailsDto(planToActivate);
    }

    @Transactional
    public void deletePlan(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("нет плана"));
        planRepository.delete(plan);
    }

    @Transactional
    public PlanDetailsDto createPlan(SavePlanRequestDto request) {
        if (planRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("План с таким именем существует");
        }
        List<Short> daysOfWeek = request.days().stream().map(PlanDayRequestDto::dayOfWeek).toList();
        Set<Short> daysSet = new HashSet<>(daysOfWeek);
        if (daysOfWeek.size() != daysSet.size()) {
            throw new IllegalArgumentException("две записи на dayOfWeek");
        }
        Set<Long> templateIdSet = new HashSet<>(request.days()
                .stream().map(PlanDayRequestDto::workoutTemplateId).toList());
        List<WorkoutTemplate> templates = workoutTemplateRepository.findAllById(templateIdSet);

        if (templateIdSet.size() != templates.size()) {
            throw new IllegalArgumentException("Одна или несколько тренировок не найдены");
        }

        Map<Long, WorkoutTemplate> map = templates.stream().collect(Collectors.
                toMap(WorkoutTemplate::getId, workoutTemplate -> workoutTemplate));

        var plan = planMapper.toEntity(request, map);

        if (planRepository.count() == 0) {
            plan.setActive(true);
        }
        var saved = planRepository.save(plan);

        return planMapper.toDetailsDto(saved);

    }

}
