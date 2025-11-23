package org.girardsimon.wealthpay.account.application;

import org.girardsimon.wealthpay.account.application.view.AccountBalanceView;
import org.girardsimon.wealthpay.account.domain.command.OpenAccount;
import org.girardsimon.wealthpay.account.domain.event.AccountOpened;
import org.girardsimon.wealthpay.account.domain.exception.AccountAlreadyExistsException;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.AccountIdGenerator;
import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.SupportedCurrency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountApplicationServiceTest {

    AccountEventStore accountEventStore = mock(AccountEventStore.class);
    AccountBalanceProjector accountBalanceProjector = mock(AccountBalanceProjector.class);

    Clock clock = Clock.fixed(
            Instant.parse("2025-11-16T15:00:00Z"),
            ZoneOffset.UTC
    );

    AccountId accountId = AccountId.newId();

    AccountIdGenerator accountIdGenerator = () -> accountId;

    AccountApplicationService accountApplicationService = new AccountApplicationService(
            accountEventStore,
            accountBalanceProjector,
            clock,
            accountIdGenerator
    );

    @Test
    void openAccount_saves_event_AccountOpened_when_account_does_not_exist() {
        // Arrange
        SupportedCurrency currency = SupportedCurrency.USD;
        Money initialBalance = new Money(BigDecimal.valueOf(10L), currency);
        OpenAccount openAccount = new OpenAccount(currency, initialBalance);
        when(accountEventStore.loadEvents(accountId)).thenReturn(List.of());

        // Act
        accountApplicationService.openAccount(openAccount);

        // Assert
        AccountOpened accountOpened = new AccountOpened(
                accountId,
                Instant.parse("2025-11-16T15:00:00Z"),
                1L,
                currency,
                initialBalance
        );
        InOrder inOrder = inOrder(accountEventStore, accountBalanceProjector);
        inOrder.verify(accountEventStore).appendEvents(accountId, 0L, List.of(accountOpened));
        inOrder.verify(accountBalanceProjector).project(List.of(accountOpened));
    }

    @Test
    void openAccount_should_not_save_event_AccountOpened_when_account_already_exists() {
        // Arrange
        SupportedCurrency currency = SupportedCurrency.USD;
        Money initialBalance = new Money(BigDecimal.valueOf(10L), currency);
        OpenAccount openAccount = new OpenAccount(currency, initialBalance);
        when(accountEventStore.loadEvents(accountId)).thenReturn(List.of(mock(AccountOpened.class)));

        // Act ... Assert
        assertThatExceptionOfType(AccountAlreadyExistsException.class)
                .isThrownBy(() -> accountApplicationService.openAccount(openAccount));
    }

    @Test
    void getAccountBalance_should_return_account_balance_view_for_given_id() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        AccountBalanceView mock = mock(AccountBalanceView.class);
        when(accountBalanceProjector.getAccountBalance(uuid)).thenReturn(mock);

        // Act
        AccountBalanceView accountBalanceView = accountApplicationService.getAccountBalance(uuid);

        // Assert
        assertThat(accountBalanceView).isEqualTo(mock);
        verifyNoInteractions(accountEventStore);
    }

}