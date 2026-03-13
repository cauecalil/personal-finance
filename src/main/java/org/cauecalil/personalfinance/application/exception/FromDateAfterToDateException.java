package org.cauecalil.personalfinance.application.exception;

public class FromDateAfterToDateException extends ApplicationException {
    public FromDateAfterToDateException() {
        super("From date cannot be after to date");
    }
}
