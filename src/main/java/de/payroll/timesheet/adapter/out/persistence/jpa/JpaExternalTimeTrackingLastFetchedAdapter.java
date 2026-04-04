package de.payroll.timesheet.adapter.out.persistence.jpa;

import de.payroll.timesheet.application.port.out.ExternalTimeTrackingLastFetchedPort;
import de.payroll.timesheet.domain.model.CompanyId;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JpaExternalTimeTrackingLastFetchedAdapter implements ExternalTimeTrackingLastFetchedPort {

    private final ExternalTimeTrackingLastFetchedEntityRepository repository;

    public JpaExternalTimeTrackingLastFetchedAdapter(ExternalTimeTrackingLastFetchedEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Instant> loadLastFetchedAt(CompanyId companyId) {
        return repository.findById(companyId.value()).map(ExternalTimeTrackingLastFetchedEntity::getLastFetchedAt);
    }

    @Override
    @Transactional
    public void saveLastFetchedAt(CompanyId companyId, Instant lastFetchedAt) {
        String id = companyId.value();
        ExternalTimeTrackingLastFetchedEntity entity =
                repository.findById(id)
                        .orElseGet(
                                () -> {
                                    ExternalTimeTrackingLastFetchedEntity e =
                                            new ExternalTimeTrackingLastFetchedEntity();
                                    e.setCompanyId(id);
                                    return e;
                                });
        entity.setLastFetchedAt(lastFetchedAt);
        repository.save(entity);
    }
}
