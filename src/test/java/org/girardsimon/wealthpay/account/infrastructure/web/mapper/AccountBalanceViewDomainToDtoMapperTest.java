package org.girardsimon.wealthpay.account.infrastructure.web.mapper;

import org.girardsimon.wealthpay.account.api.generated.model.AccountStatusDto;
import org.girardsimon.wealthpay.account.api.generated.model.AccountResponseDto;
import org.girardsimon.wealthpay.account.api.generated.model.SupportedCurrencyDto;
import org.girardsimon.wealthpay.account.application.view.AccountBalanceView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AccountBalanceViewDomainToDtoMapperTest {

    AccountBalanceViewDomainToDtoMapper mapper = new AccountBalanceViewDomainToDtoMapper();

    @Test
    void map_account_balance_view_to_dto() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100L);
        BigDecimal reserved = BigDecimal.valueOf(50L);
        AccountBalanceView accountBalanceView = new AccountBalanceView(
                accountId,
                balance,
                reserved,
                "USD",
                "OPENED",
                5L
        );

        // Act
        AccountResponseDto accountResponseDto = mapper.apply(accountBalanceView);

        // Assert
        assertAll(
                () -> assertThat(accountResponseDto.getId()).isEqualTo(accountId),
                () -> assertThat(accountResponseDto.getBalance()).isEqualTo(balance),
                () -> assertThat(accountResponseDto.getReservedAmount()).isEqualTo(reserved),
                () -> assertThat(accountResponseDto.getCurrency()).isEqualTo(SupportedCurrencyDto.USD),
                () -> assertThat(accountResponseDto.getStatus()).isEqualTo(AccountStatusDto.OPENED)
        );
    }

}