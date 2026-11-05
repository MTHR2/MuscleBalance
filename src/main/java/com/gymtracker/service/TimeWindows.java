package com.gymtracker.service;

import com.gymtracker.dto.stats.StatsPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

@Component
@RequiredArgsConstructor
public class TimeWindows {

    private final Clock clock;

    public record Window(OffsetDateTime from, OffsetDateTime to, LocalDate firstDay, LocalDate lastDay) {

        public long days() {
            return ChronoUnit.DAYS.between(firstDay, lastDay) + 1;
        }

        public double weeks() {
            return days() / 7.0;
        }
    }

    public ZoneId zone() {
        return clock.getZone();
    }

    public String zoneId() {
        return clock.getZone().getId();
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    public Window days(LocalDate firstDay, LocalDate lastDay) {
        if (lastDay.isBefore(firstDay)) {
            throw new IllegalArgumentException("Конец периода раньше начала: " + firstDay + " > " + lastDay);
        }
        return new Window(startOf(firstDay), startOf(lastDay.plusDays(1)), firstDay, lastDay);
    }

    public Window day(LocalDate date) {
        LocalDate day = date != null ? date : today();
        return days(day, day);
    }

    public Window week(LocalDate anyDay) {
        LocalDate date = anyDay != null ? anyDay : today();
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return days(monday, monday.plusDays(6));
    }

    public Window period(StatsPeriod period) {
        LocalDate today = today();
        return days(today.minusMonths(period.months()).plusDays(1), today);
    }

    public Window lastDays(int days) {
        LocalDate today = today();
        return days(today.minusDays(days - 1L), today);
    }

    public Window previous(Window window) {
        LocalDate lastDay = window.firstDay().minusDays(1);
        return days(lastDay.minusDays(window.days() - 1), lastDay);
    }

    private OffsetDateTime startOf(LocalDate day) {
        return day.atStartOfDay(clock.getZone()).toOffsetDateTime();
    }
}
