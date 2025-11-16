package org.girardsimon.wealthpay.account.domain.model;

import org.girardsimon.wealthpay.account.domain.command.CancelReservation;
import org.girardsimon.wealthpay.account.domain.command.CloseAccount;
import org.girardsimon.wealthpay.account.domain.command.CreditAccount;
import org.girardsimon.wealthpay.account.domain.command.DebitAccount;
import org.girardsimon.wealthpay.account.domain.command.OpenAccount;
import org.girardsimon.wealthpay.account.domain.command.ReserveFunds;
import org.girardsimon.wealthpay.account.domain.event.AccountClosed;
import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.event.AccountOpened;
import org.girardsimon.wealthpay.account.domain.event.FundsCredited;
import org.girardsimon.wealthpay.account.domain.event.FundsDebited;
import org.girardsimon.wealthpay.account.domain.event.FundsReserved;
import org.girardsimon.wealthpay.account.domain.event.ReservationCancelled;

import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Account {
    private final AccountId id;
    private final Currency currency;
    private Money balance;
    private AccountStatus status;
    private final Map<ReservationId, Money> reservations = new HashMap<>();
    private long version;

    private Account(AccountId id, Currency currency) {
        this.id = id;
        this.currency = currency;
    }

    public static List<AccountEvent> handle(OpenAccount openAccount) {
        if(openAccount.accountId() == null) {
            throw new IllegalArgumentException("Account id must not be null");
        }
        if(openAccount.initialBalance() == null
                || openAccount.initialBalance().isNegativeOrZero()) {
            throw new IllegalArgumentException("Initial balance must be positive");
        }
        if(!openAccount.initialBalance().currency().equals(openAccount.currency())) {
            throw new IllegalArgumentException("Initial balance currency must be the same as account currency");
        }
        AccountOpened accountOpened = new AccountOpened(
                openAccount.accountId(),
                Instant.now(),
                1L,
                openAccount.currency(),
                openAccount.initialBalance()
        );
        return List.of(accountOpened);
    }

    public List<AccountEvent> handle(CreditAccount creditAccount) {
        ensureAccountIdConsistency(creditAccount.accountId());
        checkCurrencyConsistency(creditAccount.amount().currency());
        checkStrictlyPositiveAmount(creditAccount.amount());
        ensureActive();
        FundsCredited fundsCredited = new FundsCredited(
                creditAccount.accountId(),
                Instant.now(),
                this.version + 1,
                creditAccount.amount()
        );
        return List.of(fundsCredited);
    }

    private static void checkStrictlyPositiveAmount(Money amount) {
        if(amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("Amount %s must be positive".formatted(amount));
        }
    }

    public List<AccountEvent> handle(DebitAccount debitAccount) {
        ensureAccountIdConsistency(debitAccount.accountId());
        checkCurrencyConsistency(debitAccount.amount().currency());
        checkStrictlyPositiveAmount(debitAccount.amount());
        ensureActive();
        if(debitAccount.amount().isGreaterThan(this.balance)) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        FundsDebited fundsDebited = new FundsDebited(
                debitAccount.accountId(),
                Instant.now(),
                this.version + 1,
                debitAccount.amount()
        );
        return List.of(fundsDebited);
    }

    public List<AccountEvent> handle(ReserveFunds reserveFunds) {
        if(reserveFunds.amount() == null || reserveFunds.reservationId() == null) {
            throw new IllegalArgumentException("Amount and reservationId must not be null");
        }
        ensureAccountIdConsistency(reserveFunds.accountId());
        checkCurrencyConsistency(reserveFunds.amount().currency());
        checkStrictlyPositiveAmount(reserveFunds.amount());
        ensureActive();
        if(reserveFunds.amount().isGreaterThan(getAvailableBalance())) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        if(this.reservations.containsKey(reserveFunds.reservationId())) {
            throw new IllegalArgumentException("Reservation already exists");
        }
        FundsReserved fundsReserved = new FundsReserved(
                reserveFunds.accountId(),
                Instant.now(),
                this.version + 1,
                reserveFunds.reservationId(),
                reserveFunds.amount()
        );
        return List.of(fundsReserved);
    }

    public List<AccountEvent> handle(CancelReservation cancelReservation) {
        if(cancelReservation.reservationId() == null) {
            throw new IllegalArgumentException("Reservation id must not be null");
        }
        ensureAccountIdConsistency(cancelReservation.accountId());
        if(!this.reservations.containsKey(cancelReservation.reservationId())) {
            throw new IllegalArgumentException("Reservation does not exist");
        }
        ensureActive();
        ReservationCancelled reservationCancelled = new ReservationCancelled(
                cancelReservation.accountId(),
                Instant.now(),
                this.version + 1,
                cancelReservation.reservationId()
        );
        return List.of(reservationCancelled);
    }

    public List<AccountEvent> handle(CloseAccount closeAccount) {
        ensureAccountIdConsistency(closeAccount.accountId());
        ensureActive();
        if(!this.balance.isAmountZero()) {
            throw new IllegalStateException("We cannot close an account with non zero balance");
        }
        AccountClosed accountClosed = new AccountClosed(
                closeAccount.accountId(),
                Instant.now(),
                this.version + 1
        );
        return List.of(accountClosed);
    }

    private void ensureAccountIdConsistency(AccountId accountId) {
        if(!accountId.equals(this.id)) {
            throw new IllegalArgumentException("Account id mismatch: %s vs %s"
                    .formatted(accountId, this.id));
        }
    }

    private void checkCurrencyConsistency(Currency currency) {
        if(!currency.equals(this.currency)) {
            throw new IllegalArgumentException("Currency mismatch: %s vs %s"
                    .formatted(currency, this.currency));
        }
    }

    private void ensureActive() {
        if(this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }
    }

    public static Account rehydrate(List<AccountEvent> history) {
        if(history == null || history.isEmpty()) {
            throw new IllegalArgumentException("Account history must not be null or empty");
        }
        AccountEvent firstEvent = history.getFirst();
        if(!(firstEvent instanceof AccountOpened accountOpened)) {
            throw new IllegalArgumentException("Account history must start with AccountOpened event");
        }
        Account account = new Account(accountOpened.accountId(), accountOpened.currency());
        history.forEach(account::apply);
        return account;
    }

    private void apply(AccountEvent accountEvent) {
        this.version = accountEvent.version();

        switch (accountEvent) {
            case AccountOpened accountOpened -> {
                this.balance = accountOpened.initialBalance();
                this.status = AccountStatus.ACTIVE;
            }
            case FundsCredited fundsCredited -> this.balance = this.balance.add(fundsCredited.amount());
            case AccountClosed _ -> this.status = AccountStatus.CLOSED;
            case FundsDebited fundsDebited -> this.balance = this.balance.subtract(fundsDebited.amount());
            case FundsReserved fundsReserved -> this.reservations.put(fundsReserved.reservationId(), fundsReserved.amount());
            case ReservationCancelled reservationCancelled -> this.reservations.remove(reservationCancelled.reservationId());
        }
    }

    public Money getAvailableBalance() {
        return balance.subtract(totalReservedFunds());
    }


    private Money totalReservedFunds() {
        return reservations.values().stream()
                .reduce(Money.zero(currency), Money::add);
    }

    public AccountId getId() {
        return id;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getVersion() {
        return version;
    }

    public Map<ReservationId, Money> getReservations() {
        return reservations;
    }
}
