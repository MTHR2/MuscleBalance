package com.musclebalance.mapper;

import com.musclebalance.dto.stats.MuscleStatsDto;
import com.musclebalance.dto.stats.WeekSummaryDto;
import com.musclebalance.entity.Muscle;
import org.springframework.stereotype.Component;

@Component
public class StatsMapper {

    public MuscleStatsDto toMuscleStatsDto(Muscle muscle, double sets, double reps, Short targetSets) {
        return new MuscleStatsDto(muscle.getId(), muscle.getName(), muscle.getRegionKey(),
                muscle.getMuscleGroup().getId(), muscle.getMuscleGroup().getName(),
                sets, reps, targetSets);
    }

    public WeekSummaryDto toWeekSummaryDto(int workoutsDone, Integer workoutsPlanned,
                                           int workingSets, Integer targetPercent) {

        return new WeekSummaryDto(workoutsDone, workoutsPlanned, workingSets, targetPercent);
    }

}
