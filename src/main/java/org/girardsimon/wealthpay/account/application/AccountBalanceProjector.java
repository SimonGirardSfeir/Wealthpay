package org.girardsimon.wealthpay.account.application;

import org.girardsimon.wealthpay.account.application.view.AccountBalanceView;
import org.girardsimon.wealthpay.account.domain.event.AccountEvent;

import java.util.List;
import java.util.UUID;

public interface AccountBalanceProjector {

    AccountBalanceView getAccountBalance(UUID accountId);

    void project(List<AccountEvent> events);
}
