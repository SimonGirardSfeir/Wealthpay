package org.girardsimon.wealthpay.account.application;

import org.girardsimon.wealthpay.account.application.view.AccountBalanceView;
import org.girardsimon.wealthpay.account.domain.command.OpenAccount;
import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.exception.AccountAlreadyExistsException;
import org.girardsimon.wealthpay.account.domain.model.Account;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.AccountIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccountApplicationService {

    private final AccountEventStore accountEventStore;
    private final AccountBalanceProjector accountBalanceProjector;
    private final Clock clock;
    private final AccountIdGenerator accountIdGenerator;

    public AccountApplicationService(AccountEventStore accountEventStore, AccountBalanceProjector accountBalanceProjector, Clock clock, AccountIdGenerator accountIdGenerator) {
        this.accountEventStore = accountEventStore;
        this.accountBalanceProjector = accountBalanceProjector;
        this.clock = clock;
        this.accountIdGenerator = accountIdGenerator;
    }

    @Transactional
    public AccountId openAccount(OpenAccount openAccount) {
        AccountId accountId = accountIdGenerator.newId();
        List<AccountEvent> history = accountEventStore.loadEvents(accountId);
        if(!history.isEmpty()) {
            throw new AccountAlreadyExistsException(accountId);
        }

        long expectedVersion = 0L;
        long nextVersion = expectedVersion + 1;
        List<AccountEvent> createdAccountEvents = Account.handle(openAccount, accountId, nextVersion, Instant.now(clock));
        accountEventStore.appendEvents(accountId, expectedVersion, createdAccountEvents);
        accountBalanceProjector.project(createdAccountEvents);
        return accountId;
    }

    @Transactional(readOnly = true)
    public AccountBalanceView getAccountBalance(UUID accountId) {
        return accountBalanceProjector.getAccountBalance(accountId);
    }
}
