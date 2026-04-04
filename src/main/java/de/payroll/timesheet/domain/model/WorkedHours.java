package de.payroll.timesheet.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class WorkedHours {

    private final BigDecimal value;

    private WorkedHours(BigDecimal value) {
        Objects.requireNonNull(value, "hours");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("hours must not be negative");
        }
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static WorkedHours of(BigDecimal raw) {
        return new WorkedHours(raw);
    }

    public BigDecimal asBigDecimal() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WorkedHours other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
