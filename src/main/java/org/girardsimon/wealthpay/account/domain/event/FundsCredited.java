package org.girardsimon.wealthpay.account.domain.event;

import java.time.Instant;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.EventId;
import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.TransactionId;

public record FundsCredited(
    EventId eventId,
    AccountId accountId,
    Instant occurredAt,
    long version,
    TransactionId transactionId,
    Money money)
    implements AccountEvent {}
