package org.girardsimon.wealthpay.account.infrastructure.db.repository.mapper;



import org.girardsimon.wealthpay.account.application.view.AccountBalanceView;
import org.jooq.Record6;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;

import static org.girardsimon.wealthpay.account.jooq.tables.AccountBalanceView.ACCOUNT_BALANCE_VIEW;

@Component
public class AccountBalanceViewEntryToDomainMapper implements Function<Record6<UUID, BigDecimal, BigDecimal, String, String, Long>, AccountBalanceView> {

    @Override
    public AccountBalanceView apply(Record6<UUID, BigDecimal, BigDecimal, String, String, Long> entry) {
        return new AccountBalanceView(
                entry.get(ACCOUNT_BALANCE_VIEW.ACCOUNT_ID),
                entry.get(ACCOUNT_BALANCE_VIEW.BALANCE),
                entry.get(ACCOUNT_BALANCE_VIEW.RESERVED),
                entry.get(ACCOUNT_BALANCE_VIEW.CURRENCY),
                entry.get(ACCOUNT_BALANCE_VIEW.STATUS),
                entry.get(ACCOUNT_BALANCE_VIEW.VERSION)
        );
    }
}
