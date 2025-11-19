package org.girardsimon.wealthpay.account.infrastructure.web.mapper;

import org.girardsimon.wealthpay.account.api.generated.model.OpenAccountRequestDto;
import org.girardsimon.wealthpay.account.domain.command.OpenAccount;
import org.girardsimon.wealthpay.account.domain.model.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.function.Function;

@Component
public class OpenAccountDtoToDomainMapper implements Function<OpenAccountRequestDto, OpenAccount> {

    @Override
    public OpenAccount apply(OpenAccountRequestDto openAccountRequestDto) {
        Currency currency = Currency.getInstance(openAccountRequestDto.getCurrency());
        return new OpenAccount(
                Money.of(openAccountRequestDto.getInitialBalance(), currency),
                currency
        );
    }
}
