package org.girardsimon.wealthpay.account.domain.event;

import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.ReservationId;

public record FundsReserved(AccountEventMeta meta, ReservationId reservationId, Money money)
    implements AccountEvent {}
