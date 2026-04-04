package de.payroll.timesheet.domain.model;

import java.util.Objects;

public final class WorkPackageId {

    private final String value;

    private WorkPackageId(String value) {
        this.value = Objects.requireNonNull(value, "workPackageId").trim();
        if (this.value.isEmpty()) {
            throw new IllegalArgumentException("workPackageId must not be blank");
        }
        if (this.value.length() > 64) {
            throw new IllegalArgumentException("workPackageId too long");
        }
    }

    public static WorkPackageId of(String raw) {
        return new WorkPackageId(raw);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WorkPackageId other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
