package org.cauecalil.personalfinance.presentation.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.exception.ApplicationException;
import org.cauecalil.personalfinance.domain.exception.DomainException;
import org.cauecalil.personalfinance.infrastructure.exception.InfrastructureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String URN_PREFIX = "urn:cauecalil:personalfinance:error:";

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        p.setType(URI.create(URN_PREFIX + "domain-rule-violation"));
        p.setTitle("Business Rule Violation");
        return p;
    }

    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handleApplication(ApplicationException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        p.setType(URI.create(URN_PREFIX + "application-error"));
        p.setTitle("Application Error");
        return p;
    }

    @ExceptionHandler(InfrastructureException.class)
    public ProblemDetail handleInfrastructure(InfrastructureException e) {
        log.error("Infrastructure error: {}", e.getMessage(), e);
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "We are experiencing issues communicating with external services.");
        p.setType(URI.create(URN_PREFIX + "external-service-error"));
        p.setTitle("External Service Error");
        return p;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more parameters are invalid.");
        p.setType(URI.create(URN_PREFIX + "invalid-parameter"));
        p.setTitle("Invalid Parameter");

        List<Map<String, String>> violations = e.getConstraintViolations().stream()
                .map(cv -> Map.of("field", cv.getPropertyPath().toString(), "message", cv.getMessage()))
                .collect(Collectors.toList());

        p.setProperty("violations", violations);
        return p;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred.");
        p.setType(URI.create(URN_PREFIX + "internal-server-error"));
        p.setTitle("Internal Server Error");
        return p;
    }
}
