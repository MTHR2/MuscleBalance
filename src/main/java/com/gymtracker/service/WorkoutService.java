package com.gymtracker.service;

import com.gymtracker.dto.exercise.ExerciseDetailsDto;
import com.gymtracker.dto.workout.*;
import com.gymtracker.entity.*;
import com.gymtracker.mapper.WorkoutMapper;
import com.gymtracker.repository.ExerciseRepository;
import com.gymtracker.repository.WorkoutRepository;
import com.gymtracker.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final WorkoutMapper workoutMapper;
    private final WorkoutRepository workoutRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final ExerciseRepository exerciseRepository;


    private Workout getOpenWorkout(Long workoutId) {
        Workout workout = workoutRepository.findWithExercisesById(workoutId).
                orElseThrow(() -> new NoSuchElementException("не найдена или уже завершена"));
        if (workout.getFinishedAt() != null) {
            throw new IllegalStateException("тренировка уже начата");
        }
        return workout;
    }

    private int countWorkingSets(WorkoutExercise wE) {
        return (int) wE.getSets().stream().filter(s -> !s.isWarmup()).count();
    }

    private Optional<WorkoutSet> lastWorkingSet(WorkoutExercise we) {
        return we.getSets()
                .stream().filter(s -> !s.isWarmup())
                .max(Comparator.comparing(WorkoutSet::getSetNumber));
    }

    private WorkoutExercise findWorkoutExercise(Workout workout, Long workoutExerciseId) {
        return workout.getExercises().stream()
                .filter(w -> w.getId().equals(workoutExerciseId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Упражнение не найдено в тренировке"));
    }

    private WorkoutSet findWorkoutSet(WorkoutExercise wE, Long setId) {
        return wE.getSets().stream().filter(s -> s.getId().equals(setId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Нет такого подхода в упражнении"));
    }

    private Integer calculateCompletionPercent(Workout workout) {
        int planned = 0;
        int done = 0;
        for (WorkoutExercise we : workout.getExercises()) {
            if (we.getPlannedSets() == null) {
                continue;
            }
            planned += we.getPlannedSets();
            done += Math.min(countWorkingSets(we), we.getPlannedSets());
        }
        if (planned == 0) {
            return null;
        }
        return (int) Math.round(done * 100.0 / planned);
    }

    @Transactional
    public WorkoutDetailsDto startWorkout(StartWorkoutRequestDto request) {
        if (workoutRepository.existsByFinishedAtIsNull()) {
            throw new IllegalStateException("не знаю какое исключение");
        }
        Workout workout;
        WorkoutTemplate wT;
        if (request.workoutTemplateId() != null) {
            wT = workoutTemplateRepository.findWithExercisesById(request.workoutTemplateId())
                    .orElseThrow(() -> new NoSuchElementException("шаблон не найден"));
            workout = workoutMapper.createFromTemplate(wT);  // врядли верно все это
        } else {
            String name = (request.name() == null || request.name().isBlank()) ?
                    "Свободная тренировка" : request.name().trim();
            workout = workoutMapper.createFree(name);
        }
        Workout saved = workoutRepository.save(workout);
        return workoutMapper.toDetailsDto(saved);
    }

    @Transactional(readOnly = true)
    public WorkoutDetailsDto getActiveWorkout() {
        return workoutRepository.findByFinishedAtIsNull().map(workoutMapper::toDetailsDto)
                .orElseThrow(() -> new NoSuchElementException("Нет активной тренировки"));
    }

    @Transactional(readOnly = true)
    public WorkoutDetailsDto getWorkout(Long id) {
        return workoutRepository.findWithExercisesById(id)
                .map(workoutMapper::toDetailsDto).orElseThrow(() -> new NoSuchElementException("не такой тренировки"));
    }

    @Transactional
    public void cancelWorkout(Long workoutId) {
        Workout workout = getOpenWorkout(workoutId);
        workoutRepository.delete(workout);
    }

    @Transactional
    public WorkoutExerciseDto addExercise(Long workoutId, AddWorkoutExerciseRequestDto request) {
        Workout workout = getOpenWorkout(workoutId);
        // можно всю эту провреку заменить на findById TODO
        if (!exerciseRepository.existsById(request.exerciseId())) { // доабвить потом проверку что не в архиве
            throw new NoSuchElementException("не существует такого упражнения");
        }
        if (workout.getExercises().stream().anyMatch(we -> we.getExercise().getId().equals(request.exerciseId()))) {
            throw new IllegalStateException("Это упражнене уже есть в тренировке");
        }

        Exercise exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new NoSuchElementException("упражнение не найдено"));
        short sortOrder = (short) workout.getExercises().size();
        Short targetSets = request.targetSets() != null ? request.targetSets() : (short) 3;

        WorkoutExercise wExercise = workoutMapper.createExercise(workout, exercise, targetSets, sortOrder);
        workout.getExercises().add(wExercise);
        workoutRepository.flush();
        return workoutMapper.toExerciseDto(wExercise);
    }

    @Transactional
    public WorkoutExerciseDto updateTargetSets(Long workoutId, Long wExerciseId, UpdateTargetSetsRequestDto request) {
        Workout workout = getOpenWorkout(workoutId);
        WorkoutExercise wE = findWorkoutExercise(workout, wExerciseId);
        wE.setTargetSets(request.targetSets());
        return workoutMapper.toExerciseDto(wE);

    }

    @Transactional
    public WorkoutSetDto addSet(Long workoutId, Long wExId, AddSetRequestDto request) {
        Workout workout = getOpenWorkout(workoutId);
        WorkoutExercise wE = findWorkoutExercise(workout, wExId);
        short setNumber = (short) (wE.getSets().size() + 1);
        var set = workoutMapper.createSet(wE, request, setNumber);
        wE.getSets().add(set);
        workoutRepository.flush();
        return workoutMapper.toSetDto(set);
    }

    @Transactional
    public WorkoutSetDto updateSet(Long workoutId, Long wExId, Long setId, UpdateSetRequestDto request) {
        Workout workout = getOpenWorkout(workoutId);
        WorkoutExercise wE = findWorkoutExercise(workout, wExId);
        WorkoutSet wSet = findWorkoutSet(wE, setId);
        workoutMapper.updateSet(wSet, request);
        return workoutMapper.toSetDto(wSet);
    }

    @Transactional
    public void deleteSet(Long workoutId, Long wExId, Long setId) {
        Workout workout = getOpenWorkout(workoutId);
        WorkoutExercise wE = findWorkoutExercise(workout, wExId);
        WorkoutSet wSet = findWorkoutSet(wE, setId);
        if (wE.getSets().size() != wSet.getSetNumber()) {
            throw new IllegalStateException("Это не послдений подход?");
        }
        wE.getSets().remove(wSet);
    }

    @Transactional
    public WorkoutSummaryDto finishWorkout(Long workoutId) {
        Workout workout = getOpenWorkout(workoutId);
        int completedSets = 0;
        for (WorkoutExercise we : workout.getExercises()) {
            completedSets += countWorkingSets(we);
        }
        if (completedSets == 0) {
            throw new IllegalStateException("В тренировке нет ни одного рабочего подхода, отмените её");
        }

        workout.setFinishedAt(OffsetDateTime.now());

        for (WorkoutExercise we : workout.getExercises()) {
            lastWorkingSet(we)
                    .ifPresent(s -> we.getExercise().setWorkingWeight(s.getWeight()));
        }
        long duration = Duration.between(workout.getStartedAt(), workout.getFinishedAt()).getSeconds();

        return workoutMapper.toSummaryDto(workout, duration,calculateCompletionPercent(workout) , completedSets);
    }
}
