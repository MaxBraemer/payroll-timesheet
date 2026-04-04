package de.payroll.timesheet.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "external_time_tracking_last_fetched")
public class ExternalTimeTrackingLastFetchedEntity {

    @Id
    @Column(name = "company_id", nullable = false, length = 64)
    private String companyId;

    @Column(name = "last_fetched_at", nullable = false)
    private Instant lastFetchedAt;

    public ExternalTimeTrackingLastFetchedEntity() {}

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Instant getLastFetchedAt() {
        return lastFetchedAt;
    }

    public void setLastFetchedAt(Instant lastFetchedAt) {
        this.lastFetchedAt = lastFetchedAt;
    }
}
