package org.cauecalil.personalfinance.application.exception;

public class UserCredentialNotFoundException extends ApplicationException {
    public UserCredentialNotFoundException() {
        super("No credentials configured. Please set up your API keys first.");
    }
}
