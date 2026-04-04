package de.payroll.timesheet.application.command;

import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.CompanyId;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.EmployeeId;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @param expectedVersionByWorkDate leer bei Systemimport. Sonst pro {@link LocalDate} die zuletzt bekannte
 *     Tages-{@link DailyWorkEntry#version()}; {@code 0} wenn der Tag noch nicht in der Datenbank war.
 */
public record RecordTimesheetCommand(
        CompanyId companyId,
        EmployeeId employeeId,
        List<DailyWorkEntry> days,
        BookingSource source,
        Map<LocalDate, Long> expectedVersionByWorkDate) {

    public RecordTimesheetCommand {
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("days must not be empty");
        }
        Set<LocalDate> seen = new HashSet<>();
        for (DailyWorkEntry e : days) {
            if (!seen.add(e.date())) {
                throw new IllegalArgumentException("duplicate work date: " + e.date());
            }
        }
        expectedVersionByWorkDate = Map.copyOf(expectedVersionByWorkDate);
    }
}
