package de.payroll.timesheet.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class BreakIntervalTest {

    @Test
    void gapsBetweenTwoWorkBlocksAreInterpretedAsBreaks() {
        List<WorkInterval> work =
                List.of(
                        WorkInterval.of(LocalTime.of(8, 0), LocalTime.of(12, 0), WorkPackageId.of("wp-a")),
                        WorkInterval.of(LocalTime.of(13, 0), LocalTime.of(17, 0), WorkPackageId.of("wp-b")));
        List<WorkInterval> sorted = BreakInterval.sortedWorkWithoutOverlap(work);

        List<BreakInterval> breaks = BreakInterval.gapsBetweenSortedWork(sorted);

        assertThat(breaks).hasSize(1);
        assertThat(breaks.get(0).start()).isEqualTo(LocalTime.of(12, 0));
        assertThat(breaks.get(0).end()).isEqualTo(LocalTime.of(13, 0));
        assertThat(breaks.get(0).duration().asBigDecimal()).isEqualByComparingTo(new BigDecimal("1.00"));
    }

    @Test
    void overlappingWorkIntervalsRejected() {
        List<WorkInterval> work =
                List.of(
                        WorkInterval.of(LocalTime.of(8, 0), LocalTime.of(13, 0), WorkPackageId.of("wp-a")),
                        WorkInterval.of(LocalTime.of(12, 0), LocalTime.of(17, 0), WorkPackageId.of("wp-b")));

        assertThatThrownBy(() -> BreakInterval.sortedWorkWithoutOverlap(work))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlap");
    }
}
