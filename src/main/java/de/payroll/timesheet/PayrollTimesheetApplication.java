package de.payroll.timesheet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PayrollTimesheetApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayrollTimesheetApplication.class, args);
    }
}
