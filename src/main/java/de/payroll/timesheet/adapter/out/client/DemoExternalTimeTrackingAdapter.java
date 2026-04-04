package de.payroll.timesheet.adapter.out.client;

import de.payroll.timesheet.application.port.out.ExternalTimeTrackingPort;
import de.payroll.timesheet.domain.model.CompanyId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Fiktiver Adapter zum externen Zeiterfassungssystem mit Demo-Daten. Unterstützt Delta-Abfrage über {@code modifiedSince}.
 */
@Component
public class DemoExternalTimeTrackingAdapter implements ExternalTimeTrackingPort {

    @Override
    public TimesheetsByCompany fetchTimesheetsByCompany(CompanyId companyId, Optional<Instant> modifiedSince) {
        Objects.requireNonNull(companyId, "companyId");
        Objects.requireNonNull(modifiedSince, "modifiedSince");
        Instant lastFetchedAt = Instant.now();
        if (modifiedSince.isPresent()) {
            return new TimesheetsByCompany(List.of(), lastFetchedAt);
        }
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);
        LocalDate dayInCurrentMonth = current.atDay(2);
        LocalDate dayInPreviousMonth = previous.atDay(15);
        return new TimesheetsByCompany(
                List.of(
                        new TimesheetOfEmployee(
                                "mitarbeiter-1",
                                List.of(
                                        new ExternalTimeTrackingDay(
                                                dayInCurrentMonth,
                                                List.of(
                                                        new ExternalTimeTrackingInterval(
                                                                LocalTime.of(9, 0),
                                                                LocalTime.of(12, 0),
                                                                "paket-a",
                                                                Optional.of("Freiburg"),
                                                                Optional.of("Entwicklung Lohnabrechnung")),
                                                        new ExternalTimeTrackingInterval(
                                                                LocalTime.of(13, 0),
                                                                LocalTime.of(17, 30),
                                                                "paket-b",
                                                                Optional.empty(),
                                                                Optional.empty()))),
                                        new ExternalTimeTrackingDay(
                                                dayInPreviousMonth,
                                                List.of(
                                                        new ExternalTimeTrackingInterval(
                                                                LocalTime.of(8, 0),
                                                                LocalTime.of(16, 0),
                                                                "paket-a",
                                                                Optional.empty(),
                                                                Optional.empty()))))),
                        new TimesheetOfEmployee(
                                "mitarbeiter-2",
                                List.of(
                                        new ExternalTimeTrackingDay(
                                                dayInCurrentMonth,
                                                List.of(
                                                        new ExternalTimeTrackingInterval(
                                                                LocalTime.of(10, 0),
                                                                LocalTime.of(18, 0),
                                                                "paket-c",
                                                                Optional.empty(),
                                                                Optional.empty())))))),
                lastFetchedAt);
    }
}
