package com.cauecalil.personalfinance.application.exception;

public class AccountNotFoundException extends ApplicationException {
    public AccountNotFoundException() {
        super("Account not found");
    }
}
