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
import org.girardsimon.wealthpay.account.domain.exception.AccountCurrencyMismatchException;
import org.girardsimon.wealthpay.account.domain.exception.AccountHistoryNotFound;
import org.girardsimon.wealthpay.account.domain.exception.AccountIdMismatchException;
import org.girardsimon.wealthpay.account.domain.exception.AccountInactiveException;
import org.girardsimon.wealthpay.account.domain.exception.AccountNotEmptyException;
import org.girardsimon.wealthpay.account.domain.exception.AmountMustBePositiveException;
import org.girardsimon.wealthpay.account.domain.exception.InsufficientFundsException;
import org.girardsimon.wealthpay.account.domain.exception.InvalidAccountEventStreamException;
import org.girardsimon.wealthpay.account.domain.exception.InvalidInitialBalanceException;
import org.girardsimon.wealthpay.account.domain.exception.ReservationAlreadyExistsException;
import org.girardsimon.wealthpay.account.domain.exception.ReservationNotFoundException;

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

    public static List<AccountEvent> handle(OpenAccount openAccount, AccountId accountId, Instant occurredAt) {
        if(openAccount.initialBalance() == null
                || openAccount.initialBalance().isNegativeOrZero()) {
            throw new InvalidInitialBalanceException(openAccount.initialBalance());
        }
        if(!openAccount.initialBalance().currency().equals(openAccount.currency())) {
            throw new AccountCurrencyMismatchException(openAccount.initialBalance().currency().getCurrencyCode() ,
                    openAccount.currency().getCurrencyCode());
        }
        AccountOpened accountOpened = new AccountOpened(
                accountId,
                occurredAt,
                1L,
                openAccount.currency(),
                openAccount.initialBalance()
        );
        return List.of(accountOpened);
    }

    public List<AccountEvent> handle(CreditAccount creditAccount, Instant occurredAt) {
        ensureAccountIdConsistency(creditAccount.accountId());
        checkCurrencyConsistency(creditAccount.amount().currency());
        checkStrictlyPositiveAmount(creditAccount.amount());
        ensureActive();
        FundsCredited fundsCredited = new FundsCredited(
                creditAccount.accountId(),
                occurredAt,
                this.version + 1,
                creditAccount.amount()
        );
        return List.of(fundsCredited);
    }

    private static void checkStrictlyPositiveAmount(Money amount) {
        if(amount.isNegativeOrZero()) {
            throw new AmountMustBePositiveException(amount);
        }
    }

    public List<AccountEvent> handle(DebitAccount debitAccount, Instant occurredAt) {
        ensureAccountIdConsistency(debitAccount.accountId());
        checkCurrencyConsistency(debitAccount.amount().currency());
        checkStrictlyPositiveAmount(debitAccount.amount());
        ensureActive();
        if(debitAccount.amount().isGreaterThan(this.balance)) {
            throw new InsufficientFundsException();
        }
        FundsDebited fundsDebited = new FundsDebited(
                debitAccount.accountId(),
                occurredAt,
                this.version + 1,
                debitAccount.amount()
        );
        return List.of(fundsDebited);
    }

    public List<AccountEvent> handle(ReserveFunds reserveFunds, Instant occurredAt) {
        ensureAccountIdConsistency(reserveFunds.accountId());
        checkCurrencyConsistency(reserveFunds.amount().currency());
        checkStrictlyPositiveAmount(reserveFunds.amount());
        ensureActive();
        if(reserveFunds.amount().isGreaterThan(getAvailableBalance())) {
            throw new InsufficientFundsException();
        }
        if(this.reservations.containsKey(reserveFunds.reservationId())) {
            throw new ReservationAlreadyExistsException(reserveFunds.reservationId());
        }
        FundsReserved fundsReserved = new FundsReserved(
                reserveFunds.accountId(),
                occurredAt,
                this.version + 1,
                reserveFunds.reservationId(),
                reserveFunds.amount()
        );
        return List.of(fundsReserved);
    }

    public List<AccountEvent> handle(CancelReservation cancelReservation, Instant occurredAt) {
        ensureAccountIdConsistency(cancelReservation.accountId());
        if(!this.reservations.containsKey(cancelReservation.reservationId())) {
            throw new ReservationNotFoundException(cancelReservation.reservationId());
        }
        ensureActive();
        ReservationCancelled reservationCancelled = new ReservationCancelled(
                cancelReservation.accountId(),
                occurredAt,
                this.version + 1,
                cancelReservation.reservationId()
        );
        return List.of(reservationCancelled);
    }

    public List<AccountEvent> handle(CloseAccount closeAccount, Instant occurredAt) {
        ensureAccountIdConsistency(closeAccount.accountId());
        ensureActive();
        if(!this.balance.isAmountZero()) {
            throw new AccountNotEmptyException();
        }
        AccountClosed accountClosed = new AccountClosed(
                closeAccount.accountId(),
                occurredAt,
                this.version + 1
        );
        return List.of(accountClosed);
    }

    private void ensureAccountIdConsistency(AccountId accountId) {
        if(!accountId.equals(this.id)) {
            throw new AccountIdMismatchException(accountId, this.id);
        }
    }

    private void checkCurrencyConsistency(Currency currency) {
        if(!currency.equals(this.currency)) {
            throw new AccountCurrencyMismatchException(this.currency.getCurrencyCode(), currency.getCurrencyCode());
        }
    }

    private void ensureActive() {
        if(this.status != AccountStatus.ACTIVE) {
            throw new AccountInactiveException();
        }
    }

    public static Account rehydrate(List<AccountEvent> history) {
        if(history == null || history.isEmpty()) {
            throw new AccountHistoryNotFound();
        }
        AccountEvent firstEvent = history.getFirst();
        if(!(firstEvent instanceof AccountOpened accountOpened)) {
            throw new InvalidAccountEventStreamException("Account history must start with AccountOpened event");
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
