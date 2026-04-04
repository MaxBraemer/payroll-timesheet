package de.payroll.timesheet.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Tagesbezogene Arbeitszeiten mit einem oder mehreren Intervallen (z. B. vor und nach Pause). */
public final class DailyWorkEntry implements Comparable<DailyWorkEntry> {

    private final LocalDate date;
    private final Long persistenceId;
    private final long version;
    private final List<WorkInterval> workIntervals;
    private final List<BreakInterval> breakIntervals;

    private DailyWorkEntry(
            LocalDate date,
            Long persistenceId,
            long version,
            List<WorkInterval> workIntervals,
            List<BreakInterval> breakIntervals) {
        this.date = Objects.requireNonNull(date, "date");
        this.persistenceId = persistenceId;
        this.version = version;
        Objects.requireNonNull(workIntervals, "workIntervals");
        if (workIntervals.isEmpty()) {
            throw new IllegalArgumentException("each recorded day needs at least one work interval");
        }
        this.workIntervals = List.copyOf(workIntervals);
        this.breakIntervals = List.copyOf(Objects.requireNonNull(breakIntervals, "breakIntervals"));
    }

    /**
     * Erfasst nur Arbeitsintervalle; Pausen werden in der Domäne aus den Lücken zwischen den Blöcken
     * ermittelt. Noch nicht persistiert ({@code persistenceId} leer, {@code version} 0).
     */
    public static DailyWorkEntry of(LocalDate date, List<WorkInterval> workIntervals) {
        Objects.requireNonNull(workIntervals, "workIntervals");
        if (workIntervals.isEmpty()) {
            throw new IllegalArgumentException("each recorded day needs at least one work interval");
        }
        List<WorkInterval> sorted = BreakInterval.sortedWorkWithoutOverlap(workIntervals);
        List<BreakInterval> breaks = BreakInterval.gapsBetweenSortedWork(sorted);
        return new DailyWorkEntry(date, null, 0L, sorted, breaks);
    }

    /** Wiederherstellung aus der Persistenz (ohne Neuberechnung der Pausen). */
    public static DailyWorkEntry rehydrate(
            LocalDate date,
            Long persistenceId,
            long version,
            List<WorkInterval> workIntervals,
            List<BreakInterval> breakIntervals) {
        Objects.requireNonNull(workIntervals, "workIntervals");
        if (workIntervals.isEmpty()) {
            throw new IllegalArgumentException("each recorded day needs at least one work interval");
        }
        return new DailyWorkEntry(
                date,
                persistenceId,
                version,
                workIntervals,
                breakIntervals == null ? List.of() : breakIntervals);
    }

    public LocalDate date() {
        return date;
    }

    /** Datenbank-ID des Tagesdatensatzes; leer, wenn der Tag noch nicht gespeichert ist. */
    public Long persistenceId() {
        return persistenceId;
    }

    /** Zeilenversion des Tagesdatensatzes (Optimistic Locking). */
    public long version() {
        return version;
    }

    public List<WorkInterval> workIntervals() {
        return workIntervals;
    }

    public List<BreakInterval> breakIntervals() {
        return breakIntervals;
    }

    public WorkedHours dayTotalHours() {
        BigDecimal sum = BigDecimal.ZERO;
        for (WorkInterval w : workIntervals) {
            sum = sum.add(w.duration().asBigDecimal());
        }
        return WorkedHours.of(sum);
    }

    public static List<DailyWorkEntry> sortedCopy(List<DailyWorkEntry> entries) {
        ArrayList<DailyWorkEntry> copy = new ArrayList<>(entries);
        Collections.sort(copy);
        return List.copyOf(copy);
    }

    @Override
    public int compareTo(DailyWorkEntry o) {
        return this.date.compareTo(o.date);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DailyWorkEntry other
                && date.equals(other.date)
                && workIntervals.equals(other.workIntervals)
                && breakIntervals.equals(other.breakIntervals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, workIntervals, breakIntervals);
    }
}
