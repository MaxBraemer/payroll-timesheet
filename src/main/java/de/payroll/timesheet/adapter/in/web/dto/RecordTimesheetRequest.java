package de.payroll.timesheet.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Stundenzettel mit beliebiger Tagesliste (mehrere Monate möglich). Versionsprüfung je Tag über
 * {@link DailyWorkRequest#expectedVersion()}.
 */
public record RecordTimesheetRequest(
        @NotBlank String companyId, @NotBlank String employeeId, @NotNull @Valid List<DailyWorkRequest> days) {}
