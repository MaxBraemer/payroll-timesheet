package de.payroll.timesheet.application.exception;

/** Die Buchung wurde zwischenzeitlich geändert; die mitgeschickte Version ist veraltet. */
public final class StaleBookingVersionException extends RuntimeException {

    public StaleBookingVersionException(String message) {
        super(message);
    }
}
