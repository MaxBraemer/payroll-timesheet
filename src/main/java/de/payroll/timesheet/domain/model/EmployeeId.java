package de.payroll.timesheet.domain.model;

import java.util.Objects;

public final class EmployeeId {

    private final String value;

    private EmployeeId(String value) {
        this.value = Objects.requireNonNull(value, "employeeId").trim();
        if (this.value.isEmpty()) {
            throw new IllegalArgumentException("employeeId must not be blank");
        }
        if (this.value.length() > 64) {
            throw new IllegalArgumentException("employeeId too long");
        }
    }

    public static EmployeeId of(String raw) {
        return new EmployeeId(raw);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EmployeeId other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
