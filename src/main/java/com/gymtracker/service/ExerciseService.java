package com.gymtracker.service;

import com.gymtracker.dto.exercise.CreateExerciseRequestDto;
import com.gymtracker.dto.exercise.ExerciseDetailsDto;
import com.gymtracker.dto.exercise.ExerciseListItemDto;
import com.gymtracker.dto.exercise.ExerciseMuscleRequestDto;
import com.gymtracker.entity.Exercise;
import com.gymtracker.entity.Muscle;
import com.gymtracker.entity.MuscleRole;
import com.gymtracker.exception.BusinessRuleException;
import com.gymtracker.exception.ConflictException;
import com.gymtracker.exception.DomainErrorCode;
import com.gymtracker.exception.NotFoundException;
import com.gymtracker.mapper.ExerciseMapper;
import com.gymtracker.repository.ExerciseRepository;
import com.gymtracker.repository.MuscleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final MuscleRepository muscleRepository;
    private final ExerciseMapper exerciseMapper;


    // все параметры необязательные: без них вернётся весь каталог
    @Transactional(readOnly = true)
    public List<ExerciseListItemDto> getExercises(String q, Short muscleGroupId, Boolean favourite, Boolean custom){
        String query = q == null ? "" : q.trim();
        return exerciseRepository.search(query, muscleGroupId, favourite, custom)
                .stream().map(exerciseMapper::toListItemDto).toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDetailsDto getExerciseById(Long exerciseId){
        return exerciseRepository.findById(exerciseId)
                .map(exerciseMapper::toDetailsDto)
                .orElseThrow(NotFoundException::exercise);
    }

    @Transactional
    public ExerciseDetailsDto setFavourite(Long exerciseId, boolean favourite){
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(NotFoundException::exercise);
        exercise.setFavourite(favourite);
        return exerciseMapper.toDetailsDto(exercise);
    }

    @Transactional
    public ExerciseDetailsDto createExercise(CreateExerciseRequestDto request){
        String name = request.name().trim();
        if (exerciseRepository.existsByNameIgnoreCaseAndArchivedAtIsNull(name)) {
            throw ConflictException.exerciseNameTaken(name);
        }

        List<Short> muscleIds = request.muscles().stream().map(ExerciseMuscleRequestDto::muscleId).toList();
        Set<Short> muscleIdSet = new HashSet<>(muscleIds);
        if (muscleIds.size() != muscleIdSet.size()) {
            throw new BusinessRuleException(DomainErrorCode.EXERCISE_MUSCLE_DUPLICATED);
        }
        if (request.muscles().stream().noneMatch(m -> m.role() == MuscleRole.primary)) {
            throw new BusinessRuleException(DomainErrorCode.EXERCISE_PRIMARY_MUSCLE_REQUIRED);
        }
        List<Muscle> muscleList = muscleRepository.findAllById(muscleIdSet);
        if (muscleList.size() != muscleIdSet.size()) {
            throw new BusinessRuleException(DomainErrorCode.MUSCLES_NOT_FOUND);
        }
        Map<Short, Muscle> muscles = muscleList.stream()
                .collect(Collectors.toMap(Muscle::getId, muscle -> muscle));

        Exercise saved = exerciseRepository.save(exerciseMapper.toEntity(request, muscles));
        return exerciseMapper.toDetailsDto(saved);
    }

}
