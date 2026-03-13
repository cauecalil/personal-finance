package org.cauecalil.personalfinance.infrastructure.exception;

public class PluggyException extends InfrastructureException {
    public PluggyException(String message) {
        super(message);
    }

    public PluggyException(String message, Throwable cause) {
        super(message, cause);
    }
}
