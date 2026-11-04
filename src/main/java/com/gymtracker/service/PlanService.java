package com.gymtracker.service;

import com.gymtracker.dto.plan.MuscleTargetRequestDto;
import com.gymtracker.dto.plan.PlanDayRequestDto;
import com.gymtracker.dto.plan.PlanDetailsDto;
import com.gymtracker.dto.plan.PlanListItemDto;
import com.gymtracker.dto.plan.SavePlanRequestDto;
import com.gymtracker.dto.plan.TodayWorkoutDto;
import com.gymtracker.entity.Muscle;
import com.gymtracker.entity.Plan;
import com.gymtracker.entity.WorkoutTemplate;
import com.gymtracker.mapper.PlanMapper;
import com.gymtracker.mapper.WorkoutTemplateMapper;
import com.gymtracker.repository.MuscleRepository;
import com.gymtracker.repository.PlanRepository;
import com.gymtracker.repository.WorkoutRepository;
import com.gymtracker.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    // проверяет дни запроса и загружает тренировки из библиотеки: id -> шаблон
    private Map<Long, WorkoutTemplate> loadTemplates(SavePlanRequestDto request) {
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
            throw new IllegalArgumentException("Две цели на одну мышцу");
        }
        List<Muscle> muscleList = muscleRepository.findAllById(muscleIdSet);
        if (muscleIdSet.size() != muscleList.size()) {
            throw new IllegalArgumentException("Одна или несколько мышц не найдены");
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
                .orElseThrow(() -> new NoSuchElementException("something")); // наугад пихнул исключение
    }

    /** Тренировка активного плана на сегодня; пусто, если плана нет или сегодня день отдыха */
    @Transactional(readOnly = true)
    public Optional<TodayWorkoutDto> getTodayWorkout() {
        LocalDate today = LocalDate.now();
        short dayOfWeek = (short) today.getDayOfWeek().getValue();
        OffsetDateTime from = today.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

        return planRepository.findByActiveTrue()
                .flatMap(plan -> plan.getDays().stream()
                        .filter(day -> day.getDayOfWeek() == dayOfWeek)
                        .findFirst())
                .flatMap(day -> workoutTemplateRepository.findWithExercisesById(day.getWorkoutTemplate().getId()))
                .map(template -> {
                    boolean completedToday = workoutRepository
                            .countFinishedFromTemplateBetween(template.getId(), from, from.plusDays(1)) > 0;
                    return planMapper.toTodayDto(workoutTemplateMapper.toDetailsDto(template), completedToday);
                });
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
                .orElseThrow(() -> new NoSuchElementException("нет плана"));
        // имя занято другим планом (смена регистра у самого себя не в счёт)
        if (!plan.getName().equalsIgnoreCase(request.name())
                && planRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("План с таким именем существует");
        }
        Map<Long, WorkoutTemplate> templates = loadTemplates(request);
        Map<Short, Muscle> muscles = loadMuscles(request);

        planMapper.updateEntity(plan, request, templates, muscles);
        planRepository.flush();
        return planMapper.toDetailsDto(plan);
    }

}
