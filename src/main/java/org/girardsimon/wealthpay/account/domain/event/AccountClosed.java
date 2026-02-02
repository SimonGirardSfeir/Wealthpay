package org.girardsimon.wealthpay.account.domain.event;

import java.time.Instant;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.EventId;

public record AccountClosed(EventId eventId, AccountId accountId, Instant occurredAt, long version)
    implements AccountEvent {}
