package com.gymtracker.service;

import com.gymtracker.dto.muscle.MuscleGroupCardDto;
import com.gymtracker.mapper.MuscleGroupMapper;
import com.gymtracker.repository.MuscleGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MuscleGroupService {
    private final MuscleGroupRepository muscleGroupRepository;
    private final MuscleGroupMapper muscleGroupMapper;

    public List<MuscleGroupCardDto> getAllMuscleGroups(){
        return muscleGroupRepository.findAll().
                stream().map(muscleGroupMapper::toMuscleGroupCardDto).toList();
    }
    public MuscleGroupCardDto getMuscleGroupById(Short muscleGroupId){
        return muscleGroupRepository.findById(muscleGroupId).
                map(muscleGroupMapper::toMuscleGroupCardDto).orElseThrow();
    }
}
