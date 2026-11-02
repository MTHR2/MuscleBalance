package com.gymtracker.service;

import com.gymtracker.dto.ExerciseDetailsDto;
import com.gymtracker.dto.ExerciseListItemDto;
import com.gymtracker.mapper.ExerciseMapper;
import com.gymtracker.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;


    @Transactional(readOnly = true)
    public List<ExerciseListItemDto> getExercisesForMuscleGroup(Short muscleGroupId){
        return exerciseRepository.findByPrimaryMuscleGroup(muscleGroupId).stream().map(exerciseMapper::toListItemDto).toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDetailsDto getExerciseById(Long exerciseId){
        return exerciseRepository.findById(exerciseId)
                .map(exerciseMapper::toDetailsDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Упражнение не найдено"));
    }

}
