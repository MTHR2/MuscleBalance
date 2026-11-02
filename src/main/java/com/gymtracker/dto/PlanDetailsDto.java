package com.gymtracker.dto;

import java.util.List;

public record PlanDetailsDto (Long id,
                              String name,
                              String description,
                              boolean active,
                              List<PlanDayDto> days){
}
