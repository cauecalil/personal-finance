package org.cauecalil.personalfinance.application.exception;

public class ListTransactionsFromDateAfterToDateException extends ApplicationException {
    public ListTransactionsFromDateAfterToDateException() {
        super("From date cannot be after to date");
    }
}
