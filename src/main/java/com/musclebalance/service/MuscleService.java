package com.musclebalance.service;

import com.musclebalance.dto.muscle.MuscleItemDto;
import com.musclebalance.mapper.MuscleMapper;
import com.musclebalance.repository.MuscleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MuscleService {
    private final MuscleRepository muscleRepository;
    private final MuscleMapper muscleMapper;

    // все мышцы по порядку групп, внутри группы по порядку мышц
    @Transactional(readOnly = true)
    public List<MuscleItemDto> getAllMuscles() {
        return muscleRepository.findAllByOrderByMuscleGroup_SortOrderAscSortOrderAsc()
                .stream().map(muscleMapper::toMuscleItemDto).toList();
    }
}
