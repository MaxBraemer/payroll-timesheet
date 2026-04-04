package de.payroll.timesheet.adapter.out.persistence;

import de.payroll.timesheet.adapter.out.persistence.jpa.TimesheetDayEntity;
import de.payroll.timesheet.adapter.out.persistence.jpa.TimesheetDayEntityRepository;
import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.CompanyId;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.EmployeeId;
import de.payroll.timesheet.domain.model.RecordedTimesheet;
import de.payroll.timesheet.domain.repository.TimesheetRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TimesheetJpaAdapter implements TimesheetRepository {

    private final TimesheetDayEntityRepository timesheetDayEntityRepository;
    private final TimesheetDayPersistenceMapper mapper;

    public TimesheetJpaAdapter(
            TimesheetDayEntityRepository timesheetDayEntityRepository, TimesheetDayPersistenceMapper mapper) {
        this.timesheetDayEntityRepository = timesheetDayEntityRepository;
        this.mapper = mapper;
    }

    @Override
    public List<DailyWorkEntry> findForUpdate(CompanyId companyId, EmployeeId employeeId, Set<LocalDate> workDates) {
        if (workDates.isEmpty()) {
            return List.of();
        }
        return timesheetDayEntityRepository
                .findForUpdateByWorkDates(companyId.value(), employeeId.value(), workDates)
                .stream()
                .map(mapper::toDomainDay)
                .toList();
    }

    @Override
    public RecordedTimesheet saveSubmittedDays(
            CompanyId companyId,
            EmployeeId employeeId,
            List<DailyWorkEntry> days,
            BookingSource source,
            Instant updatedAt) {
        String c = companyId.value();
        String e = employeeId.value();
        Set<LocalDate> dates = days.stream().map(DailyWorkEntry::date).collect(Collectors.toSet());
        List<TimesheetDayEntity> locked = timesheetDayEntityRepository.findForUpdateByWorkDates(c, e, dates);
        Map<LocalDate, TimesheetDayEntity> byDate =
                locked.stream().collect(Collectors.toMap(TimesheetDayEntity::getWorkDate, d -> d));

        for (DailyWorkEntry day : days.stream().sorted().toList()) {
            TimesheetDayEntity entity = byDate.get(day.date());
            if (entity == null) {
                entity = new TimesheetDayEntity();
            }
            mapper.applyTimesheetDay(entity, day, companyId, employeeId, source, updatedAt);
            TimesheetDayEntity saved = timesheetDayEntityRepository.save(entity);
            byDate.put(day.date(), saved);
        }

        List<TimesheetDayEntity> reloaded =
                timesheetDayEntityRepository.findForUpdateByWorkDates(c, e, dates).stream()
                        .sorted(Comparator.comparing(TimesheetDayEntity::getWorkDate))
                        .toList();
        List<DailyWorkEntry> domainDays = new ArrayList<>();
        for (TimesheetDayEntity row : reloaded) {
            domainDays.add(mapper.toDomainDay(row));
        }
        BookingSource lastBy = reloaded.isEmpty() ? source : reloaded.get(reloaded.size() - 1).getLastUpdatedBy();
        return RecordedTimesheet.of(companyId, employeeId, domainDays, lastBy, updatedAt);
    }
}
