package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.SupportedCurrency;

public record AccountOpened(AccountEventMeta meta, SupportedCurrency currency, Money initialBalance)
    implements AccountEvent {}
