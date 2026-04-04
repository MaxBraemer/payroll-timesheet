package de.payroll.timesheet.application.port.out;

import de.payroll.timesheet.domain.model.CompanyId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Ausgehender Port: externes Zeiterfassungssystem. Unterstützt optionale Delta-Abfragen über
 * {@code modifiedSince}; {@link TimesheetsByCompany#lastFetchedAt()} nach erfolgreichem Lauf über
 * {@link ExternalTimeTrackingLastFetchedPort} persistieren.
 */
public interface ExternalTimeTrackingPort {

    TimesheetsByCompany fetchTimesheetsByCompany(CompanyId companyId, Optional<Instant> modifiedSince);

    /** Ein Eintrag entspricht einem Mitarbeitenden und den eingetragenen Zeiten für beliebig viele Tage. */
    record TimesheetOfEmployee(String employeeId, List<ExternalTimeTrackingDay> days) {}

    record ExternalTimeTrackingDay(LocalDate date, List<ExternalTimeTrackingInterval> intervals) {}

    record ExternalTimeTrackingInterval(
            LocalTime start,
            LocalTime end,
            String workPackageId,
            Optional<String> workLocation,
            Optional<String> description) {}

    record TimesheetsByCompany(List<TimesheetOfEmployee> lines, Instant lastFetchedAt) {}
}
