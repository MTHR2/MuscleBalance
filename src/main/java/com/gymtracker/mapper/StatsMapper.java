package com.gymtracker.mapper;

import com.gymtracker.dto.stats.MuscleStatsDto;
import com.gymtracker.dto.stats.WeekSummaryDto;
import com.gymtracker.entity.Muscle;
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
