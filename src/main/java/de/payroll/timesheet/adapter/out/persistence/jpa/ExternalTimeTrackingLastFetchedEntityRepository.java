package de.payroll.timesheet.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalTimeTrackingLastFetchedEntityRepository
        extends JpaRepository<ExternalTimeTrackingLastFetchedEntity, String> {}
