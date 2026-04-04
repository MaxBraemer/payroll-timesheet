package de.payroll.timesheet.adapter.out.persistence;

import de.payroll.timesheet.adapter.out.persistence.jpa.TimesheetDayEntity;
import de.payroll.timesheet.adapter.out.persistence.jpa.WorkIntervalEntity;
import de.payroll.timesheet.adapter.out.persistence.jpa.WorkIntervalKind;
import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.BreakInterval;
import de.payroll.timesheet.domain.model.CompanyId;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.EmployeeId;
import de.payroll.timesheet.domain.model.WorkInterval;
import de.payroll.timesheet.domain.model.WorkPackageId;
import de.payroll.timesheet.domain.model.WorkedHours;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class TimesheetDayPersistenceMapper {

    void applyTimesheetDay(
            TimesheetDayEntity entity,
            DailyWorkEntry day,
            CompanyId companyId,
            EmployeeId employeeId,
            BookingSource source,
            Instant when) {
        entity.setCompanyId(companyId.value());
        entity.setEmployeeId(employeeId.value());
        entity.setWorkDate(day.date());
        entity.setCalendarYear(day.date().getYear());
        entity.setCalendarMonth(day.date().getMonthValue());
        entity.setHoursWorked(day.dayTotalHours().asBigDecimal());
        entity.setLastUpdatedBy(source);
        entity.setUpdatedAt(when);
        replaceIntervals(entity, day);
    }

    DailyWorkEntry toDomainDay(TimesheetDayEntity d) {
        List<WorkIntervalEntity> intervalEntities = new ArrayList<>(d.getIntervals());
        intervalEntities.sort(Comparator.comparing(WorkIntervalEntity::getStartTime));
        List<WorkInterval> work = new ArrayList<>();
        List<BreakInterval> breaks = new ArrayList<>();
        for (WorkIntervalEntity i : intervalEntities) {
            WorkIntervalKind kind = i.getIntervalKind() == null ? WorkIntervalKind.WORK : i.getIntervalKind();
            if (kind == WorkIntervalKind.BREAK) {
                breaks.add(
                        BreakInterval.of(
                                i.getStartTime(),
                                i.getEndTime(),
                                WorkedHours.of(i.getDurationHours())));
            } else {
                String pkg = i.getWorkPackageId();
                if (pkg == null || pkg.isBlank()) {
                    throw new IllegalStateException(
                            "work interval row missing work_package_id for work_day_id=" + d.getId());
                }
                work.add(
                        WorkInterval.of(
                                i.getStartTime(),
                                i.getEndTime(),
                                WorkPackageId.of(pkg),
                                Optional.ofNullable(i.getWorkLocation()),
                                Optional.ofNullable(i.getDescriptionText())));
            }
        }
        work.sort(Comparator.comparing(WorkInterval::start));
        breaks.sort(Comparator.comparing(BreakInterval::start));
        return DailyWorkEntry.rehydrate(d.getWorkDate(), d.getId(), d.getVersion(), work, breaks);
    }

    private static void replaceIntervals(TimesheetDayEntity entity, DailyWorkEntry day) {
        entity.getIntervals().clear();
        List<PersistedIntervalRow> rows = new ArrayList<>();
        for (WorkInterval interval : day.workIntervals()) {
            rows.add(
                    new PersistedIntervalRow(
                            WorkIntervalKind.WORK,
                            interval.start(),
                            interval.end(),
                            interval.duration().asBigDecimal(),
                            interval.workPackageId().value(),
                            interval.workLocation().orElse(null),
                            interval.description().orElse(null)));
        }
        for (BreakInterval interval : day.breakIntervals()) {
            rows.add(
                    new PersistedIntervalRow(
                            WorkIntervalKind.BREAK,
                            interval.start(),
                            interval.end(),
                            interval.duration().asBigDecimal(),
                            null,
                            null,
                            null));
        }
        rows.sort(Comparator.comparing(PersistedIntervalRow::start));
        for (PersistedIntervalRow r : rows) {
            WorkIntervalEntity i = new WorkIntervalEntity();
            i.setTimesheetDay(entity);
            i.setIntervalKind(r.kind());
            i.setStartTime(r.start());
            i.setEndTime(r.end());
            i.setDurationHours(r.hours());
            i.setWorkPackageId(r.workPackageId());
            i.setWorkLocation(r.workLocation());
            i.setDescriptionText(r.descriptionText());
            entity.getIntervals().add(i);
        }
    }

    private record PersistedIntervalRow(
            WorkIntervalKind kind,
            LocalTime start,
            LocalTime end,
            BigDecimal hours,
            String workPackageId,
            String workLocation,
            String descriptionText) {}
}
