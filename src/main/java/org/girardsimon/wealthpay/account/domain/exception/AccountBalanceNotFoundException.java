package org.girardsimon.wealthpay.account.domain.exception;

import java.util.UUID;

public class AccountBalanceNotFoundException extends RuntimeException {
    public AccountBalanceNotFoundException(UUID accountId) {
        super("No balance found for account %s".formatted(accountId));
    }
}
