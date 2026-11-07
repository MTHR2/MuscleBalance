package com.musclebalance.service;
import com.musclebalance.dto.workouttemplate.*;
import com.musclebalance.entity.Exercise;
import com.musclebalance.entity.WorkoutTemplate;
import com.musclebalance.exception.BusinessRuleException;
import com.musclebalance.exception.ConflictException;
import com.musclebalance.exception.DomainErrorCode;
import com.musclebalance.exception.NotFoundException;
import com.musclebalance.mapper.WorkoutTemplateMapper;
import com.musclebalance.repository.ExerciseRepository;
import com.musclebalance.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutTemplateService {
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final WorkoutTemplateMapper workoutTemplateMapper;
    private final ExerciseRepository exerciseRepository;

    @Transactional(readOnly = true)
    public List<WorkoutTemplateListItemDto> getWorkoutList (){
        return workoutTemplateRepository.findAllByOrderByNameAsc().stream()
                .map(workoutTemplateMapper::toListItemDto).toList();
    }

    @Transactional(readOnly = true)
    public WorkoutTemplateDetailsDto getWorkoutTemplateDetailsById (Long workoutTemplateId){
        return workoutTemplateRepository.findWithExercisesById(workoutTemplateId)
                .map(workoutTemplateMapper::toDetailsDto)
                .orElseThrow(NotFoundException::template);
    }


    @Transactional
    public WorkoutTemplateDetailsDto createWorkoutTemplate (CreateWorkoutTemplateRequestDto requestDto){
        if (workoutTemplateRepository.existsByNameIgnoreCase(requestDto.name())){
            throw ConflictException.templateNameTaken(requestDto.name());
        }

        List<Long> idList =  requestDto.exercises().stream().
                map(WorkoutTemplateExerciseRequestDto::exerciseId).toList();

        Set<Long> idSet = new HashSet<>(idList);

        if (idList.size()!= idSet.size()){
            throw new BusinessRuleException(DomainErrorCode.TEMPLATE_EXERCISE_DUPLICATED);
        }

        List<Exercise> exercises = exerciseRepository.findAllById(idList);

        if(exercises.size() != idList.size()){
            throw new BusinessRuleException(DomainErrorCode.EXERCISES_NOT_FOUND);
        }

        Map<Long, Exercise> map = exercises.stream().
                collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        WorkoutTemplate workoutTemplate = workoutTemplateMapper.toEntity(requestDto,map);
        WorkoutTemplate saved = workoutTemplateRepository.save(workoutTemplate);
        return workoutTemplateMapper.toDetailsDto(saved);
    }


}
