package com.gymtracker.dto.bodyweight;

import java.math.BigDecimal;
import java.util.List;

public record BodyWeightChartDto(List<BodyWeightPointDto> points,
                                 BigDecimal current,
                                 BigDecimal change30,
                                 BigDecimal target,
                                 BigDecimal toTarget) {
}
