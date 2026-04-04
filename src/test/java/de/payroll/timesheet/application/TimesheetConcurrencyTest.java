package de.payroll.timesheet.application;

import static org.assertj.core.api.Assertions.assertThat;

import de.payroll.timesheet.adapter.out.persistence.jpa.TimesheetDayEntityRepository;
import de.payroll.timesheet.application.command.RecordTimesheetCommand;
import de.payroll.timesheet.application.service.RecordTimesheetService;
import de.payroll.timesheet.domain.model.BookingSource;
import de.payroll.timesheet.domain.model.CompanyId;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.EmployeeId;
import de.payroll.timesheet.domain.model.WorkInterval;
import de.payroll.timesheet.domain.model.WorkPackageId;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TimesheetConcurrencyTest {

    @Autowired private RecordTimesheetService recordTimesheetService;
    @Autowired private TimesheetDayEntityRepository timesheetDayEntityRepository;

    @Test
    void parallelUpsertsForSameWorkDateYieldSingleRow() throws Exception {
        CompanyId company = CompanyId.of("concurrency-test-co");
        EmployeeId employee = EmployeeId.of("emp-1");
        LocalDate workDate = LocalDate.of(2026, 4, 10);
        timesheetDayEntityRepository.deleteAll();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int hour = 8 + (i % 8);
            Callable<Void> task =
                    () -> {
                        WorkInterval interval =
                                WorkInterval.of(
                                        LocalTime.of(hour, 0),
                                        LocalTime.of(hour + 1, 0),
                                        WorkPackageId.of("pkg"));
                        DailyWorkEntry day = DailyWorkEntry.of(workDate, List.of(interval));
                        recordTimesheetService.recordTimesheet(
                                new RecordTimesheetCommand(
                                        company, employee, List.of(day), BookingSource.MANUAL_WEB, Map.of()));
                        return null;
                    };
            futures.add(pool.submit(task));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertThat(timesheetDayEntityRepository.findAll()).hasSize(1);
    }
}
