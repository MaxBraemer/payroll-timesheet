package de.payroll.timesheet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Erfasste Stunden eines Mitarbeiters (beliebige Datumsliste). */
public record RecordedTimesheet(
        CompanyId companyId,
        EmployeeId employeeId,
        List<DailyWorkEntry> days,
        WorkedHours totalHours,
        BookingSource lastUpdatedBy,
        Instant updatedAt) {

    public static RecordedTimesheet of(
            CompanyId companyId,
            EmployeeId employeeId,
            List<DailyWorkEntry> days,
            BookingSource lastUpdatedBy,
            Instant updatedAt) {
        List<DailyWorkEntry> sorted = DailyWorkEntry.sortedCopy(days);
        BigDecimal sum = BigDecimal.ZERO;
        for (DailyWorkEntry d : sorted) {
            sum = sum.add(d.dayTotalHours().asBigDecimal());
        }
        return new RecordedTimesheet(
                companyId, employeeId, sorted, WorkedHours.of(sum), lastUpdatedBy, updatedAt);
    }
}
