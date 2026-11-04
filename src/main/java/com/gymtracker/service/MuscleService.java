package com.gymtracker.service;

import com.gymtracker.dto.muscle.MuscleItemDto;
import com.gymtracker.mapper.MuscleMapper;
import com.gymtracker.repository.MuscleRepository;
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
