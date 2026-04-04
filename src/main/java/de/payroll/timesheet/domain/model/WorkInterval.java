package de.payroll.timesheet.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Arbeitsintervall an einem Kalendertag. Die Dauer wird ausschließlich aus Beginn und Ende abgeleitet
 * (Stunden mit zwei Nachkommastellen, halb aufgerundet).
 */
public final class WorkInterval {

    public static final int MAX_WORK_LOCATION_LENGTH = 128;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final LocalTime start;
    private final LocalTime end;
    private final WorkedHours duration;
    private final WorkPackageId workPackageId;
    private final Optional<String> workLocation;
    private final Optional<String> description;

    private WorkInterval(
            LocalTime start,
            LocalTime end,
            WorkPackageId workPackageId,
            Optional<String> workLocation,
            Optional<String> description) {
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.workPackageId = Objects.requireNonNull(workPackageId, "workPackageId");
        this.workLocation = normalizeOptionalText(workLocation, MAX_WORK_LOCATION_LENGTH, "workLocation");
        this.description = normalizeOptionalText(description, MAX_DESCRIPTION_LENGTH, "description");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end on the same calendar day");
        }
        BigDecimal hours = decimalHoursBetween(start, end);
        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("work interval duration must be positive");
        }
        this.duration = WorkedHours.of(hours);
    }

    public static WorkInterval of(LocalTime start, LocalTime end, WorkPackageId workPackageId) {
        return new WorkInterval(start, end, workPackageId, Optional.empty(), Optional.empty());
    }

    public static WorkInterval of(
            LocalTime start,
            LocalTime end,
            WorkPackageId workPackageId,
            Optional<String> workLocation,
            Optional<String> description) {
        return new WorkInterval(start, end, workPackageId, workLocation, description);
    }

    static BigDecimal decimalHoursBetween(LocalTime start, LocalTime end) {
        return BigDecimal.valueOf(Duration.between(start, end).toSeconds())
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private static Optional<String> normalizeOptionalText(Optional<String> raw, int maxLen, String label) {
        Objects.requireNonNull(raw, label);
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        String t = raw.get().trim();
        if (t.isEmpty()) {
            return Optional.empty();
        }
        if (t.length() > maxLen) {
            throw new IllegalArgumentException(label + " too long (max " + maxLen + " characters)");
        }
        return Optional.of(t);
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

    public WorkPackageId workPackageId() {
        return workPackageId;
    }

    public Optional<String> workLocation() {
        return workLocation;
    }

    public Optional<String> description() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WorkInterval other
                && start.equals(other.start)
                && end.equals(other.end)
                && workPackageId.equals(other.workPackageId)
                && workLocation.equals(other.workLocation)
                && description.equals(other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, workPackageId, workLocation, description);
    }
}
