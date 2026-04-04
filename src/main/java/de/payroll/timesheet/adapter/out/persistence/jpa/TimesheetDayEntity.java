package de.payroll.timesheet.adapter.out.persistence.jpa;

import de.payroll.timesheet.domain.model.BookingSource;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(
        name = "work_day",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_employee_work_date",
                        columnNames = {"company_id", "employee_id", "work_date"})
        })
public class TimesheetDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, length = 64)
    private String companyId;

    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @Column(name = "calendar_year", nullable = false)
    private int calendarYear;

    @Column(name = "calendar_month", nullable = false)
    private int calendarMonth;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "hours_worked", nullable = false, precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_updated_by", nullable = false, length = 32)
    private BookingSource lastUpdatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "timesheetDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    @BatchSize(size = 64)
    private List<WorkIntervalEntity> intervals = new ArrayList<>();

    public TimesheetDayEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public int getCalendarYear() {
        return calendarYear;
    }

    public void setCalendarYear(int calendarYear) {
        this.calendarYear = calendarYear;
    }

    public int getCalendarMonth() {
        return calendarMonth;
    }

    public void setCalendarMonth(int calendarMonth) {
        this.calendarMonth = calendarMonth;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public BookingSource getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(BookingSource lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<WorkIntervalEntity> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<WorkIntervalEntity> intervals) {
        this.intervals = intervals;
    }
}
