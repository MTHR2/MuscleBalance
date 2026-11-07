package com.musclebalance.dto.plan;

import java.util.List;

public record PlanDetailsDto (Long id,
                              String name,
                              String description,
                              boolean active,
                              List<PlanDayDto> days,
                              Short defaultWeeklySets,
                              List<MuscleTargetDto> muscleTargets){
}
