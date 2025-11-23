package org.girardsimon.wealthpay.account.application.view;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceView(
        UUID accountId,
        BigDecimal balance,
        BigDecimal reservedFunds,
        String currency,
        String status,
        long version
) {
}
