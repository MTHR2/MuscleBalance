package com.musclebalance.service;

import com.musclebalance.dto.plan.MuscleTargetRequestDto;
import com.musclebalance.dto.plan.PlanDayRequestDto;
import com.musclebalance.dto.plan.PlanDetailsDto;
import com.musclebalance.dto.plan.PlanListItemDto;
import com.musclebalance.dto.plan.SavePlanRequestDto;
import com.musclebalance.dto.plan.TodayWorkoutDto;
import com.musclebalance.entity.Muscle;
import com.musclebalance.entity.Plan;
import com.musclebalance.entity.WorkoutTemplate;
import com.musclebalance.exception.BusinessRuleException;
import com.musclebalance.exception.ConflictException;
import com.musclebalance.exception.DomainErrorCode;
import com.musclebalance.exception.NotFoundException;
import com.musclebalance.mapper.PlanMapper;
import com.musclebalance.mapper.WorkoutTemplateMapper;
import com.musclebalance.repository.MuscleRepository;
import com.musclebalance.repository.PlanRepository;
import com.musclebalance.repository.WorkoutRepository;
import com.musclebalance.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanMapper planMapper;
    private final PlanRepository planRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final MuscleRepository muscleRepository;
    private final WorkoutTemplateMapper workoutTemplateMapper;
    private final WorkoutRepository workoutRepository;
    private final TimeWindows timeWindows;

    // проверяет дни запроса и загружает тренировки из библиотеки: id -> шаблон
    private Map<Long, WorkoutTemplate> loadTemplates(SavePlanRequestDto request) {
        List<Short> daysOfWeek = request.days().stream().map(PlanDayRequestDto::dayOfWeek).toList();
        Set<Short> daysSet = new HashSet<>(daysOfWeek);
        if (daysOfWeek.size() != daysSet.size()) {
            throw new BusinessRuleException(DomainErrorCode.PLAN_DAY_DUPLICATED);
        }
        Set<Long> templateIdSet = new HashSet<>(request.days()
                .stream().map(PlanDayRequestDto::workoutTemplateId).toList());
        List<WorkoutTemplate> templates = workoutTemplateRepository.findAllById(templateIdSet);

        if (templateIdSet.size() != templates.size()) {
            throw new BusinessRuleException(DomainErrorCode.TEMPLATES_NOT_FOUND);
        }
        return templates.stream().collect(Collectors.
                toMap(WorkoutTemplate::getId, workoutTemplate -> workoutTemplate));
    }

    // проверяет цели запроса и загружает мышцы: id -> мышца
    private Map<Short, Muscle> loadMuscles(SavePlanRequestDto request) {
        List<MuscleTargetRequestDto> targets = request.muscleTargets() != null
                ? request.muscleTargets() : List.of();
        List<Short> muscleIds = targets.stream().map(MuscleTargetRequestDto::muscleId).toList();
        Set<Short> muscleIdSet = new HashSet<>(muscleIds);
        if (muscleIds.size() != muscleIdSet.size()) {
            throw new BusinessRuleException(DomainErrorCode.PLAN_MUSCLE_TARGET_DUPLICATED);
        }
        List<Muscle> muscleList = muscleRepository.findAllById(muscleIdSet);
        if (muscleIdSet.size() != muscleList.size()) {
            throw new BusinessRuleException(DomainErrorCode.MUSCLES_NOT_FOUND);
        }
        return muscleList.stream()
                .collect(Collectors.toMap(Muscle::getId, muscle -> muscle));
    }

    @Transactional(readOnly = true)
    public List<PlanListItemDto> getPlanList() {
        return planRepository.findAllByOrderByActiveDescNameAsc().
                stream().map(planMapper::toListItemDto).toList();
    }

    @Transactional(readOnly = true)
    public PlanDetailsDto getPlan(Long planId) {
        return planRepository.findWithDaysById(planId).map(planMapper::toDetailsDto)
                .orElseThrow(NotFoundException::plan);
    }

    /** Тренировка активного плана на сегодня; пусто, если плана нет или сегодня день отдыха */
    @Transactional(readOnly = true)
    public Optional<TodayWorkoutDto> getTodayWorkout() {
        TimeWindows.Window today = timeWindows.day(null);
        short dayOfWeek = (short) today.firstDay().getDayOfWeek().getValue();

        return planRepository.findByActiveTrue()
                .flatMap(plan -> plan.getDays().stream()
                        .filter(day -> day.getDayOfWeek() == dayOfWeek)
                        .findFirst())
                .flatMap(day -> workoutTemplateRepository.findWithExercisesById(day.getWorkoutTemplate().getId()))
                .map(template -> {
                    boolean completedToday = workoutRepository
                            .countFinishedFromTemplateBetween(template.getId(), today.from(), today.to()) > 0;
                    return planMapper.toTodayDto(workoutTemplateMapper.toDetailsDto(template), completedToday);
                });
    }

    @Transactional
    public PlanDetailsDto activatePlan(Long id) {
        Plan planToActivate = planRepository.findWithDaysById(id).orElseThrow(NotFoundException::plan);
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
                .orElseThrow(NotFoundException::plan);
        planRepository.delete(plan);
    }

    @Transactional
    public PlanDetailsDto createPlan(SavePlanRequestDto request) {
        if (planRepository.existsByNameIgnoreCase(request.name())) {
            throw ConflictException.planNameTaken(request.name());
        }
        Map<Long, WorkoutTemplate> templates = loadTemplates(request);
        Map<Short, Muscle> muscles = loadMuscles(request);

        var plan = planMapper.toEntity(request, templates, muscles);

        if (planRepository.count() == 0) {
            plan.setActive(true);
        }
        var saved = planRepository.save(plan);

        return planMapper.toDetailsDto(saved);
    }

    /** Изменение плана влияет только на будущие тренировки: начатые и завершённые хранят свою копию упражнений */
    @Transactional
    public PlanDetailsDto updatePlan(Long id, SavePlanRequestDto request) {
        Plan plan = planRepository.findWithDaysById(id)
                .orElseThrow(NotFoundException::plan);
        // имя занято другим планом (смена регистра у самого себя не в счёт)
        if (!plan.getName().equalsIgnoreCase(request.name())
                && planRepository.existsByNameIgnoreCase(request.name())) {
            throw ConflictException.planNameTaken(request.name());
        }
        Map<Long, WorkoutTemplate> templates = loadTemplates(request);
        Map<Short, Muscle> muscles = loadMuscles(request);

        planMapper.updateEntity(plan, request, templates, muscles);
        planRepository.flush();
        return planMapper.toDetailsDto(plan);
    }

}
