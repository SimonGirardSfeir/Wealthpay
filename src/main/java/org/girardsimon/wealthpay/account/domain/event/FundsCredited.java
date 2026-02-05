package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.TransactionId;

public record FundsCredited(AccountEventMeta meta, TransactionId transactionId, Money money)
    implements AccountEvent {}
