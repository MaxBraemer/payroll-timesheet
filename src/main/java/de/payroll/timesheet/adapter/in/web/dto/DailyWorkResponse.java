package de.payroll.timesheet.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyWorkResponse(
        LocalDate date,
        Long dayId,
        long dayVersion,
        int year,
        int month,
        List<WorkIntervalResponse> intervals,
        List<WorkIntervalResponse> pauses) {}
