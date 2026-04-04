package de.payroll.timesheet.adapter.in.web;

import de.payroll.timesheet.application.exception.StaleBookingVersionException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException e) {
        String msg =
                e.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .orElse("validation failed");
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> conflict(ObjectOptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Concurrent update detected; please retry."));
    }

    @ExceptionHandler(StaleBookingVersionException.class)
    public ResponseEntity<Map<String, String>> staleVersion(StaleBookingVersionException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
