package com.cauecalil.personalfinance.application.exception;

public class BankConnectionAlreadyExistsException extends ApplicationException {
    public BankConnectionAlreadyExistsException() {
        super("Bank connection already exists for this item");
    }
}
