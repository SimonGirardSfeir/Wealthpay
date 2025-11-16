package org.girardsimon.wealthpay.account.domain.command;

import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.Money;

import java.util.Currency;

public record OpenAccount(
        AccountId accountId,
        Money initialBalance,
        Currency currency
) {
}
