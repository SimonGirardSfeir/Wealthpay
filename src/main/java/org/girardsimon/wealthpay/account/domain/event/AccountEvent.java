package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.AccountId;

import java.time.Instant;

public sealed interface AccountEvent permits AccountOpened,
        FundsCredited,
        FundsDebited,
        FundsReserved,
        ReservationCancelled,
        AccountClosed {

    AccountId accountId();
    Instant occurredAt();
    long version();
}
