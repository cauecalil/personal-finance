package com.cauecalil.personalfinance.application.exception;

public class BankConnectionNotFoundException extends ApplicationException {
    public BankConnectionNotFoundException() {
        super("Bank connection not found");
    }
}
