package de.payroll.timesheet.domain.model;

import java.util.Objects;

public final class CompanyId {

    private final String value;

    private CompanyId(String value) {
        this.value = Objects.requireNonNull(value, "companyId").trim();
        if (this.value.isEmpty()) {
            throw new IllegalArgumentException("companyId must not be blank");
        }
        if (this.value.length() > 64) {
            throw new IllegalArgumentException("companyId too long");
        }
    }

    public static CompanyId of(String raw) {
        return new CompanyId(raw);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CompanyId other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
