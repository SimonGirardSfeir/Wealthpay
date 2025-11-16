package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.AccountId;

import java.time.Instant;

public record AccountClosed(
        AccountId accountId,
        Instant occurredAt,
        long version
) implements AccountEvent {
}
