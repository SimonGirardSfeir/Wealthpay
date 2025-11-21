package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.SupportedCurrency;

import java.time.Instant;

public record AccountOpened(
        AccountId accountId,
        Instant occurredAt,
        long version,
        SupportedCurrency currency,
        Money initialBalance
)
        implements AccountEvent {
}
