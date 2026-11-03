package com.gymtracker.service;
import com.gymtracker.dto.workouttemplate.*;
import com.gymtracker.entity.Exercise;
import com.gymtracker.entity.WorkoutTemplate;
import com.gymtracker.mapper.WorkoutTemplateMapper;
import com.gymtracker.repository.ExerciseRepository;
import com.gymtracker.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тренировка не найдена"));
    }


    @Transactional
    public WorkoutTemplateDetailsDto createWorkoutTemplate (CreateWorkoutTemplateRequestDto requestDto){
        if (workoutTemplateRepository.existsByNameIgnoreCase(requestDto.name())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Тренировка с таким названием уже есть");
        }

        List<Long> idList =  requestDto.exercises().stream().
                map(WorkoutTemplateExerciseRequestDto::exerciseId).toList();

        Set<Long> idSet = new HashSet<>(idList);

        if (idList.size()!= idSet.size()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Упражнение добавлено в тренировку дважды");
        }

        List<Exercise> exercises = exerciseRepository.findAllById(idList);

        if(exercises.size() != idList.size()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Одно или несколько упражнений не найдены");
        }

        Map<Long, Exercise> map = exercises.stream().
                collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        WorkoutTemplate workoutTemplate = workoutTemplateMapper.toEntity(requestDto,map);
        WorkoutTemplate saved = workoutTemplateRepository.save(workoutTemplate);
        return workoutTemplateMapper.toDetailsDto(saved);
    }


}
