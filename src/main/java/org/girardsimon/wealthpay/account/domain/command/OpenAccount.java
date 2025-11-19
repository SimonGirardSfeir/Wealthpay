package org.girardsimon.wealthpay.account.domain.command;

import org.girardsimon.wealthpay.account.domain.model.Money;

import java.util.Currency;

public record OpenAccount(
        Money initialBalance,
        Currency currency
) {
}
