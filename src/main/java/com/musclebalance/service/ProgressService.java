package com.musclebalance.service;

import com.musclebalance.dto.stats.ExerciseProgressDto;
import com.musclebalance.dto.stats.ExerciseStatsItemDto;
import com.musclebalance.dto.stats.PersonalRecordDto;
import com.musclebalance.dto.stats.PersonalRecordsDto;
import com.musclebalance.dto.stats.ProgressPointDto;
import com.musclebalance.dto.stats.StatsPeriod;
import com.musclebalance.entity.Exercise;
import com.musclebalance.entity.LoadType;
import com.musclebalance.repository.ExerciseRepository;
import com.musclebalance.repository.StatsRepository;
import com.musclebalance.repository.StatsRepository.BestSetRow;
import com.musclebalance.repository.StatsRepository.RecordRow;
import com.musclebalance.repository.WorkoutRepository;
import com.musclebalance.service.TimeWindows.Window;
import com.musclebalance.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private static final BigDecimal EPLEY_REPS = BigDecimal.valueOf(30);

    private final StatsRepository statsRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final TimeWindows timeWindows;

    @Transactional(readOnly = true)
    public List<ExerciseStatsItemDto> getExercises(StatsPeriod period) {
        Window window = timeWindows.period(period);
        return workoutRepository.findExercisesWithWorkingSetsBetween(window.from(), window.to()).stream()
                .map(e -> new ExerciseStatsItemDto(e.getId(), e.getName(), e.getImageUrl(),
                        e.getLoadType(), e.getWorkingWeight()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseProgressDto getProgress(Long exerciseId, StatsPeriod period) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(NotFoundException::exercise);
        boolean bodyweight = exercise.getLoadType() == LoadType.bodyweight;
        Window window = timeWindows.period(period);

        List<BestSetRow> weeks = statsRepository.findBestSetByWeek(exerciseId, window.from(), window.to(),
                timeWindows.zoneId());
        BigDecimal best = statsRepository.findBestMetricBefore(exerciseId, window.from());

        List<ProgressPointDto> points = new ArrayList<>();
        for (BestSetRow row : weeks) {
            BigDecimal metric = metric(row.getWeight(), row.getReps(), bodyweight);
            boolean record = best != null && metric.compareTo(best) > 0;
            if (best == null || metric.compareTo(best) > 0) {
                best = metric;
            }
            points.add(new ProgressPointDto(row.getWeekStart(), row.getWeight(), row.getReps(), record));
        }

        if (points.isEmpty()) {
            return new ExerciseProgressDto(exercise.getId(), exercise.getName(), exercise.getLoadType(),
                    points, null, null, null, null);
        }

        ProgressPointDto first = points.getFirst();
        ProgressPointDto last = points.getLast();
        BigDecimal current = metric(last.weight(), last.reps(), bodyweight);
        BigDecimal start = metric(first.weight(), first.reps(), bodyweight);
        BigDecimal delta = current.subtract(start);
        Integer deltaPercent = start.signum() == 0 ? null
                : delta.multiply(BigDecimal.valueOf(100)).divide(start, 0, RoundingMode.HALF_UP).intValue();
        BigDecimal estimatedMax = bodyweight ? null : epley(last.weight(), last.reps());

        return new ExerciseProgressDto(exercise.getId(), exercise.getName(), exercise.getLoadType(),
                points, current, delta, deltaPercent, estimatedMax);
    }

    @Transactional(readOnly = true)
    public PersonalRecordsDto getRecords(StatsPeriod period, int limit) {
        Window window = timeWindows.period(period);
        List<RecordRow> rows = statsRepository.findRecords(window.from(), window.to(), timeWindows.zoneId());
        List<PersonalRecordDto> records = rows.stream()
                .limit(limit)
                .map(r -> new PersonalRecordDto(r.getExerciseId(), r.getName(), LoadType.valueOf(r.getLoadType()),
                        r.getDate(), r.getWeight(), r.getReps(), r.getDelta()))
                .toList();
        return new PersonalRecordsDto(records, rows.size());
    }

    private BigDecimal metric(BigDecimal weight, short reps, boolean bodyweight) {
        return bodyweight ? BigDecimal.valueOf(reps) : weight;
    }

    private BigDecimal epley(BigDecimal weight, short reps) {
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(reps).divide(EPLEY_REPS, 6, RoundingMode.HALF_UP));
        return weight.multiply(factor).setScale(1, RoundingMode.HALF_UP);
    }
}
