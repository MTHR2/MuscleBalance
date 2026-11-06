package com.gymtracker.dto.stats;

import java.util.List;

public record PersonalRecordsDto(List<PersonalRecordDto> records,
                                 int totalInPeriod) {
}
