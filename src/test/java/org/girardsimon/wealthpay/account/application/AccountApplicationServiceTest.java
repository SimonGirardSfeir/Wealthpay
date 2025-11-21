package org.girardsimon.wealthpay.account.application;

import org.girardsimon.wealthpay.account.domain.command.OpenAccount;
import org.girardsimon.wealthpay.account.domain.event.AccountOpened;
import org.girardsimon.wealthpay.account.domain.exception.AccountAlreadyExistsException;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.AccountIdGenerator;
import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.SupportedCurrency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountApplicationServiceTest {

    AccountEventStore accountEventStore = mock(AccountEventStore.class);

    Clock clock = Clock.fixed(
            Instant.parse("2025-11-16T15:00:00Z"),
            ZoneOffset.UTC
    );

    AccountId accountId = AccountId.newId();

    AccountIdGenerator accountIdGenerator = () -> accountId;

    AccountApplicationService accountApplicationService = new AccountApplicationService(
            accountEventStore,
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
        verify(accountEventStore).appendEvents(accountId, 0L, List.of(accountOpened));
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

}