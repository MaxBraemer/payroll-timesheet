package de.payroll.timesheet.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Pause innerhalb eines Kalendertags, abgeleitet aus der Lücke zwischen zwei Arbeitsintervallen.
 */
public final class BreakInterval {

    private final LocalTime start;
    private final LocalTime end;
    private final WorkedHours duration;

    private BreakInterval(LocalTime start, LocalTime end, WorkedHours duration) {
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.duration = Objects.requireNonNull(duration, "duration");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end on the same calendar day");
        }
        BigDecimal expectedHours =
                BigDecimal.valueOf(Duration.between(start, end).toSeconds())
                        .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        if (duration.asBigDecimal().compareTo(expectedHours) != 0) {
            throw new IllegalArgumentException(
                    "duration must equal end minus start (rounded to 2 decimal hours), expected "
                            + expectedHours);
        }
    }

    public static BreakInterval of(LocalTime start, LocalTime end, WorkedHours duration) {
        return new BreakInterval(start, end, duration);
    }

    /** Sortiert nach Beginn und prüft, dass sich Arbeitsintervalle nicht überschneiden. */
    public static List<WorkInterval> sortedWorkWithoutOverlap(List<WorkInterval> workIntervals) {
        Objects.requireNonNull(workIntervals, "workIntervals");
        List<WorkInterval> sorted =
                workIntervals.stream()
                        .sorted(Comparator.comparing(WorkInterval::start))
                        .toList();
        validateNoOverlap(sorted);
        return sorted;
    }

    /**
     * Pausen nur als Lücken zwischen aufeinanderfolgenden Arbeitsintervallen (nicht vor dem ersten oder
     * nach dem letzten Block). {@code sortedWork} muss zeitlich sortiert und überschneidungsfrei sein.
     */
    public static List<BreakInterval> gapsBetweenSortedWork(List<WorkInterval> sortedWork) {
        Objects.requireNonNull(sortedWork, "sortedWork");
        if (sortedWork.size() <= 1) {
            return List.of();
        }
        List<BreakInterval> breaks = new ArrayList<>();
        for (int i = 0; i < sortedWork.size() - 1; i++) {
            LocalTime gapStart = sortedWork.get(i).end();
            LocalTime gapEnd = sortedWork.get(i + 1).start();
            if (gapStart.isBefore(gapEnd)) {
                BigDecimal hours = WorkInterval.decimalHoursBetween(gapStart, gapEnd);
                breaks.add(BreakInterval.of(gapStart, gapEnd, WorkedHours.of(hours)));
            }
        }
        return List.copyOf(breaks);
    }

    private static void validateNoOverlap(List<WorkInterval> sorted) {
        for (int i = 0; i < sorted.size() - 1; i++) {
            if (sorted.get(i).end().isAfter(sorted.get(i + 1).start())) {
                throw new IllegalArgumentException("work intervals must not overlap");
            }
        }
    }

    public LocalTime start() {
        return start;
    }

    public LocalTime end() {
        return end;
    }

    public WorkedHours duration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BreakInterval other
                && start.equals(other.start)
                && end.equals(other.end)
                && duration.equals(other.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, duration);
    }
}
