package de.payroll.timesheet.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "work_interval")
public class WorkIntervalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_day_id", nullable = false)
    private TimesheetDayEntity timesheetDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "interval_kind", length = 16)
    private WorkIntervalKind intervalKind;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_hours", nullable = false, precision = 10, scale = 2)
    private BigDecimal durationHours;

    /** Nur bei {@link WorkIntervalKind#WORK}; bei Pausen ({@code BREAK}) {@code null}. */
    @Column(name = "work_package_id", length = 64)
    private String workPackageId;

    @Column(name = "work_location", length = 128)
    private String workLocation;

    @Column(name = "description_text", length = 2000)
    private String descriptionText;

    public WorkIntervalEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimesheetDayEntity getTimesheetDay() {
        return timesheetDay;
    }

    public void setTimesheetDay(TimesheetDayEntity timesheetDay) {
        this.timesheetDay = timesheetDay;
    }

    public WorkIntervalKind getIntervalKind() {
        return intervalKind;
    }

    public void setIntervalKind(WorkIntervalKind intervalKind) {
        this.intervalKind = intervalKind;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(BigDecimal durationHours) {
        this.durationHours = durationHours;
    }

    public String getWorkPackageId() {
        return workPackageId;
    }

    public void setWorkPackageId(String workPackageId) {
        this.workPackageId = workPackageId;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }
}
