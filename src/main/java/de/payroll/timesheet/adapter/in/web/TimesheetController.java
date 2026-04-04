package de.payroll.timesheet.adapter.in.web;

import de.payroll.timesheet.adapter.in.web.dto.DailyWorkRequest;
import de.payroll.timesheet.adapter.in.web.dto.RecordTimesheetRequest;
import de.payroll.timesheet.adapter.in.web.dto.RecordTimesheetResponse;
import de.payroll.timesheet.adapter.in.web.dto.WorkIntervalRequest;
import de.payroll.timesheet.application.command.RecordTimesheetCommand;
import de.payroll.timesheet.application.service.RecordTimesheetService;
import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.CompanyId;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.EmployeeId;
import de.payroll.timesheet.domain.model.WorkInterval;
import de.payroll.timesheet.domain.model.WorkPackageId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/timesheets")
public class TimesheetController {

    private final RecordTimesheetService recordTimesheetService;

    public TimesheetController(RecordTimesheetService recordTimesheetService) {
        this.recordTimesheetService = recordTimesheetService;
    }

    @PostMapping
    public RecordTimesheetResponse recordTimesheet(@Valid @RequestBody RecordTimesheetRequest request) {
        List<DailyWorkEntry> domainDays = request.days().stream().map(TimesheetController::toDomainDay).toList();
        var expectedByDate =
                request.days().stream()
                        .collect(Collectors.toMap(DailyWorkRequest::date, DailyWorkRequest::expectedVersion));
        RecordTimesheetCommand command =
                new RecordTimesheetCommand(
                        CompanyId.of(request.companyId()),
                        EmployeeId.of(request.employeeId()),
                        domainDays,
                        BookingSource.MANUAL_WEB,
                        expectedByDate);
        return RecordTimesheetResponse.from(recordTimesheetService.recordTimesheet(command));
    }

    private static DailyWorkEntry toDomainDay(DailyWorkRequest d) {
        List<WorkInterval> intervals = d.intervals().stream().map(TimesheetController::toDomainInterval).toList();
        return DailyWorkEntry.of(d.date(), intervals);
    }

    private static WorkInterval toDomainInterval(WorkIntervalRequest r) {
        return WorkInterval.of(
                r.start(),
                r.end(),
                WorkPackageId.of(r.workPackageId()),
                r.workLocation() == null || r.workLocation().isBlank()
                        ? Optional.empty()
                        : Optional.of(r.workLocation()),
                r.description() == null || r.description().isBlank()
                        ? Optional.empty()
                        : Optional.of(r.description()));
    }
}
