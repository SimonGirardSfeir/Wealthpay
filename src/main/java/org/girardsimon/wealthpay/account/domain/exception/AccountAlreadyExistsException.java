package org.girardsimon.wealthpay.account.domain.exception;

import org.girardsimon.wealthpay.account.domain.model.AccountId;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(AccountId accountId) {
        super("Account %s already exists".formatted(accountId));
    }
}
