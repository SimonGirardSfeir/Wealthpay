package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.Money;

import java.time.Instant;
import java.util.Currency;

public record AccountOpened(
        AccountId accountId,
        Instant occurredAt,
        long version,
        Currency currency,
        Money initialBalance
)
        implements AccountEvent {
}
