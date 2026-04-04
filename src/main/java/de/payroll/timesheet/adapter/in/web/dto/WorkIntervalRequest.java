package de.payroll.timesheet.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record WorkIntervalRequest(
        @NotNull LocalTime start,
        @NotNull LocalTime end,
        @NotBlank @Size(max = 64) String workPackageId,
        @Size(max = 128) String workLocation,
        @Size(max = 2000) String description) {}
