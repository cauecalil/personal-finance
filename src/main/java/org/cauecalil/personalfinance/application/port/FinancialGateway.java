package org.cauecalil.personalfinance.application.port;

import org.cauecalil.personalfinance.domain.model.UserCredential;

public interface FinancialGateway {
    String createConnectionToken(UserCredential userCredential, String itemId);
}
