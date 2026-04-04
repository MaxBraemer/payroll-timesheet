package de.payroll.timesheet.adapter.in.scheduler;

import de.payroll.timesheet.application.command.RecordTimesheetCommand;
import de.payroll.timesheet.application.port.out.ExternalTimeTrackingLastFetchedPort;
import de.payroll.timesheet.application.port.out.ExternalTimeTrackingPort;
import de.payroll.timesheet.application.port.out.ExternalTimeTrackingPort.ExternalTimeTrackingDay;
import de.payroll.timesheet.application.port.out.ExternalTimeTrackingPort.ExternalTimeTrackingInterval;
import de.payroll.timesheet.application.port.out.ExternalTimeTrackingPort.TimesheetOfEmployee;
import de.payroll.timesheet.application.port.out.ExternalTimeTrackingPort.TimesheetsByCompany;
import de.payroll.timesheet.application.service.RecordTimesheetService;
import de.payroll.timesheet.config.ExternalTimeTrackingProperties;
import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.CompanyId;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.EmployeeId;
import de.payroll.timesheet.domain.model.WorkInterval;
import de.payroll.timesheet.domain.model.WorkPackageId;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExternalTimeTrackingImportScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExternalTimeTrackingImportScheduler.class);

    private final ExternalTimeTrackingPort externalTimeTrackingPort;
    private final ExternalTimeTrackingLastFetchedPort externalTimeTrackingLastFetched;
    private final RecordTimesheetService recordTimesheetService;
    private final ExternalTimeTrackingProperties externalTimeTrackingProperties;

    public ExternalTimeTrackingImportScheduler(
            ExternalTimeTrackingPort externalTimeTrackingPort,
            ExternalTimeTrackingLastFetchedPort externalTimeTrackingLastFetched,
            RecordTimesheetService recordTimesheetService,
            ExternalTimeTrackingProperties externalTimeTrackingProperties) {
        this.externalTimeTrackingPort = externalTimeTrackingPort;
        this.externalTimeTrackingLastFetched = externalTimeTrackingLastFetched;
        this.recordTimesheetService = recordTimesheetService;
        this.externalTimeTrackingProperties = externalTimeTrackingProperties;
    }

    @Scheduled(fixedDelayString = "${payroll.external-time-tracking.import-interval-ms:15000}")
    public void importFromExternalSystem() {
        CompanyId companyId =
                CompanyId.of(
                        externalTimeTrackingProperties.getDemoCompanyId()); // In einem echten Szenario könnte man hier mehrere Unternehmen unterstützen
        TimesheetsByCompany batch =
                externalTimeTrackingPort.fetchTimesheetsByCompany(
                        companyId, externalTimeTrackingLastFetched.loadLastFetchedAt(companyId));
        boolean allLinesSucceeded = true;
        for (TimesheetOfEmployee line : batch.lines()) {
            try {
                if (line.days().isEmpty()) {
                    log.debug(
                            "Importzeile ohne Tage übersprungen: company={} employee={}",
                            companyId.value(),
                            line.employeeId());
                    continue;
                }
                List<DailyWorkEntry> dailyEntries =
                        line.days().stream().map(ExternalTimeTrackingImportScheduler::toDailyEntry).toList();
                RecordTimesheetCommand command =
                        new RecordTimesheetCommand(
                                companyId,
                                EmployeeId.of(line.employeeId()),
                                dailyEntries,
                                BookingSource.TIME_TRACKING_IMPORT,
                                Map.of());
                recordTimesheetService.recordTimesheet(command);
                log.info(
                        "Zeiterfassung übernommen: company={} employee={} ({} Tage)",
                        companyId.value(),
                        line.employeeId(),
                        line.days().size());
            } catch (Exception e) {
                allLinesSucceeded = false;
                log.warn("Import fehlgeschlagen: company={} employee={}", companyId.value(), line.employeeId(), e);
            }
        }
        if (allLinesSucceeded) {
            externalTimeTrackingLastFetched.saveLastFetchedAt(companyId, batch.lastFetchedAt());
        }
    }

    private static DailyWorkEntry toDailyEntry(ExternalTimeTrackingDay day) {
        List<WorkInterval> intervals =
                day.intervals().stream().map(ExternalTimeTrackingImportScheduler::toWorkInterval).toList();
        return DailyWorkEntry.of(day.date(), intervals);
    }

    private static WorkInterval toWorkInterval(ExternalTimeTrackingInterval i) {
        return WorkInterval.of(
                i.start(),
                i.end(),
                WorkPackageId.of(i.workPackageId()),
                i.workLocation(),
                i.description());
    }
}
