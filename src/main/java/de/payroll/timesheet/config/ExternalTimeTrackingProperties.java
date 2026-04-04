package de.payroll.timesheet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payroll.external-time-tracking")
public class ExternalTimeTrackingProperties {

    private long importIntervalMs = 15_000L;
    private String demoCompanyId = "demo-firma-1";

    public long getImportIntervalMs() {
        return importIntervalMs;
    }

    public void setImportIntervalMs(long importIntervalMs) {
        this.importIntervalMs = importIntervalMs;
    }

    public String getDemoCompanyId() {
        return demoCompanyId;
    }

    public void setDemoCompanyId(String demoCompanyId) {
        this.demoCompanyId = demoCompanyId;
    }
}
