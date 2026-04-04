package de.payroll.timesheet.domain.repository;

import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.CompanyId;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.EmployeeId;
import de.payroll.timesheet.domain.model.RecordedTimesheet;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TimesheetRepository {

    List<DailyWorkEntry> findForUpdate(CompanyId companyId, EmployeeId employeeId, Set<LocalDate> workDates);

    RecordedTimesheet saveSubmittedDays(
            CompanyId companyId,
            EmployeeId employeeId,
            List<DailyWorkEntry> days,
            BookingSource source,
            Instant updatedAt);
}
