package com.gymtracker.dto.plan;

import java.util.List;

public record PlanDetailsDto (Long id,
                              String name,
                              String description,
                              boolean active,
                              List<PlanDayDto> days){
}
