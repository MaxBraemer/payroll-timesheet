package de.payroll.timesheet.application.service;

import de.payroll.timesheet.application.command.RecordTimesheetCommand;
import de.payroll.timesheet.application.exception.StaleBookingVersionException;
import de.payroll.timesheet.domain.model.DailyWorkEntry;
import de.payroll.timesheet.domain.model.RecordedTimesheet;
import de.payroll.timesheet.domain.repository.TimesheetRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class RecordTimesheetService {

    private static final int MAX_UPSERT_ATTEMPTS = 5;

    private final TimesheetRepository timesheetRepository;
    private final TransactionTemplate transactionTemplate;

    public RecordTimesheetService(TimesheetRepository timesheetRepository, TransactionTemplate transactionTemplate) {
        this.timesheetRepository = timesheetRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public RecordedTimesheet recordTimesheet(RecordTimesheetCommand command) {
        Instant now = Instant.now();
        Set<LocalDate> dates =
                command.days().stream().map(DailyWorkEntry::date).collect(Collectors.toSet());
        for (int attempt = 0; attempt < MAX_UPSERT_ATTEMPTS; attempt++) {
            try {
                return transactionTemplate.execute(status -> upsertInTransaction(command, dates, now));
            } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_UPSERT_ATTEMPTS - 1) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("upsert failed after retries");
    }

    private RecordedTimesheet upsertInTransaction(RecordTimesheetCommand command, Set<LocalDate> dates, Instant now) {
        List<DailyWorkEntry> lockedExisting =
                timesheetRepository.findForUpdate(command.companyId(), command.employeeId(), dates);
        assertVersions(command.expectedVersionByWorkDate(), command.days(), lockedExisting);
        return timesheetRepository.saveSubmittedDays(
                command.companyId(), command.employeeId(), command.days(), command.source(), now);
    }

    private static void assertVersions(
            Map<LocalDate, Long> expectedByDate,
            List<DailyWorkEntry> submitted,
            List<DailyWorkEntry> existingRows) {
        if (expectedByDate.isEmpty()) {
            return;
        }
        Map<LocalDate, DailyWorkEntry> existingByDate = new HashMap<>();
        for (DailyWorkEntry d : existingRows) {
            existingByDate.put(d.date(), d);
        }
        for (DailyWorkEntry submittedDay : submitted) {
            LocalDate d = submittedDay.date();
            if (!expectedByDate.containsKey(d)) {
                throw new IllegalArgumentException("expectedVersion fehlt für " + d);
            }
            long want = expectedByDate.get(d);
            DailyWorkEntry row = existingByDate.get(d);
            if (row != null) {
                if (row.version() != want) {
                    throw new StaleBookingVersionException(
                            "Tag "
                                    + d
                                    + ": Daten wurden inzwischen geändert (erwartete Version "
                                    + want
                                    + ", aktuell "
                                    + row.version()
                                    + "). Bitte neu laden.");
                }
            } else if (want != 0L) {
                throw new StaleBookingVersionException(
                        "Tag "
                                + d
                                + ": kein gespeicherter Eintrag; für neue Tage expectedVersion=0 verwenden "
                                + "(übergeben: "
                                + want
                                + ").");
            }
        }
    }
}
