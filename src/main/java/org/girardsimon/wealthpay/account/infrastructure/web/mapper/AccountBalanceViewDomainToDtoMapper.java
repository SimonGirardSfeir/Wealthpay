package org.girardsimon.wealthpay.account.infrastructure.web.mapper;

import org.girardsimon.wealthpay.account.api.generated.model.AccountResponseDto;
import org.girardsimon.wealthpay.account.api.generated.model.SupportedCurrencyDto;
import org.girardsimon.wealthpay.account.api.generated.model.AccountStatusDto;
import org.girardsimon.wealthpay.account.application.view.AccountBalanceView;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class AccountBalanceViewDomainToDtoMapper implements Function<AccountBalanceView, AccountResponseDto> {
    @Override
    public AccountResponseDto apply(AccountBalanceView accountBalanceView) {
        return new AccountResponseDto()
                .id(accountBalanceView.accountId())
                .balance(accountBalanceView.balance())
                .reservedAmount(accountBalanceView.reservedFunds())
                .currency(SupportedCurrencyDto.valueOf(accountBalanceView.currency()))
                .status(AccountStatusDto.valueOf(accountBalanceView.status()));
    }
}
