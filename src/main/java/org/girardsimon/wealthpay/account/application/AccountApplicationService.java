package org.girardsimon.wealthpay.account.application;

import org.girardsimon.wealthpay.account.domain.command.OpenAccount;
import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.model.Account;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class AccountApplicationService {

    private final AccountEventStore accountEventStore;
    private final Clock clock;

    public AccountApplicationService(AccountEventStore accountEventStore, Clock clock) {
        this.accountEventStore = accountEventStore;
        this.clock = clock;
    }

    @Transactional
    public void openAccount(OpenAccount openAccount) {
        AccountId accountId = openAccount.accountId();
        List<AccountEvent> history = accountEventStore.loadEvents(accountId);
        if(!history.isEmpty()) {
            throw new IllegalStateException("Account %s already exists".formatted(accountId));
        }

        List<AccountEvent> createdAccountEvents = Account.handle(openAccount, Instant.now(clock));
        accountEventStore.appendEvents(accountId, 1L, createdAccountEvents);
    }
}
