package de.payroll.timesheet.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record DailyWorkRequest(
        @NotNull LocalDate date,
        @Min(0) long expectedVersion,
        @NotEmpty @Valid List<WorkIntervalRequest> intervals) {}
