package de.payroll.timesheet.adapter.in.web.dto;

import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.BreakInterval;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.RecordedTimesheet;
import de.payroll.timesheet.domain.model.WorkInterval;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RecordTimesheetResponse(
        String companyId,
        String employeeId,
        BigDecimal totalHoursWorked,
        List<DailyWorkResponse> days,
        BookingSource lastUpdatedBy,
        Instant updatedAt) {

    public static RecordTimesheetResponse from(RecordedTimesheet recorded) {
        List<DailyWorkResponse> days =
                recorded.days().stream().map(RecordTimesheetResponse::toDayResponse).toList();
        return new RecordTimesheetResponse(
                recorded.companyId().value(),
                recorded.employeeId().value(),
                recorded.totalHours().asBigDecimal(),
                days,
                recorded.lastUpdatedBy(),
                recorded.updatedAt());
    }

    private static DailyWorkResponse toDayResponse(DailyWorkEntry d) {
        int y = d.date().getYear();
        int m = d.date().getMonthValue();
        return new DailyWorkResponse(
                d.date(),
                d.persistenceId(),
                d.version(),
                y,
                m,
                d.workIntervals().stream().map(RecordTimesheetResponse::toWorkInterval).toList(),
                d.breakIntervals().stream().map(RecordTimesheetResponse::toPauseInterval).toList());
    }

    private static WorkIntervalResponse toWorkInterval(WorkInterval i) {
        return new WorkIntervalResponse(
                i.start(),
                i.end(),
                i.duration().asBigDecimal(),
                i.workPackageId().value(),
                i.workLocation().orElse(null),
                i.description().orElse(null));
    }

    private static WorkIntervalResponse toPauseInterval(BreakInterval i) {
        return new WorkIntervalResponse(i.start(), i.end(), i.duration().asBigDecimal(), null, null, null);
    }
}
