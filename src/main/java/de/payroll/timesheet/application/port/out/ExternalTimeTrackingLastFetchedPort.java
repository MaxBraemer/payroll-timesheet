package de.payroll.timesheet.application.port.out;

import de.payroll.timesheet.domain.model.CompanyId;
import java.time.Instant;
import java.util.Optional;

/**
 * Speichert {@code lastFetchedAt} pro Firma: Grenze für die nächste Delta-Abfrage beim externen Zeiterfassungssystem.
 */
public interface ExternalTimeTrackingLastFetchedPort {

    Optional<Instant> loadLastFetchedAt(CompanyId companyId);

    void saveLastFetchedAt(CompanyId companyId, Instant lastFetchedAt);
}
