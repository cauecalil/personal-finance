package org.cauecalil.personalfinance.infrastructure.exception;

public class PluggyAuthException extends InfrastructureException {
    public PluggyAuthException(String message) {
        super(message);
    }

    public PluggyAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
