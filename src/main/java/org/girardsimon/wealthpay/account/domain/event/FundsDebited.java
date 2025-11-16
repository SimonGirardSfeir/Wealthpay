package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.Money;

import java.time.Instant;

public record FundsDebited(
        AccountId accountId,
        Instant occurredAt,
        long version,
        Money amount
) implements AccountEvent {
}
