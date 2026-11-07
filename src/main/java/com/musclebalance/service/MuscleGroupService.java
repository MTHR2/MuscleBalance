package com.musclebalance.service;

import com.musclebalance.dto.muscle.MuscleGroupCardDto;
import com.musclebalance.exception.NotFoundException;
import com.musclebalance.mapper.MuscleGroupMapper;
import com.musclebalance.repository.MuscleGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MuscleGroupService {
    private final MuscleGroupRepository muscleGroupRepository;
    private final MuscleGroupMapper muscleGroupMapper;

    public List<MuscleGroupCardDto> getAllMuscleGroups(){
        return muscleGroupRepository.findAllByOrderBySortOrderAsc().
                stream().map(muscleGroupMapper::toMuscleGroupCardDto).toList();
    }
    public MuscleGroupCardDto getMuscleGroupById(Short muscleGroupId){
        return muscleGroupRepository.findById(muscleGroupId).
                map(muscleGroupMapper::toMuscleGroupCardDto).orElseThrow(NotFoundException::muscleGroup);
    }
}
