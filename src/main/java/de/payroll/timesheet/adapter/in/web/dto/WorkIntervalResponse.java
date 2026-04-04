package de.payroll.timesheet.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record WorkIntervalResponse(
        LocalTime start,
        LocalTime end,
        BigDecimal durationHours,
        String workPackageId,
        String workLocation,
        String description) {}
