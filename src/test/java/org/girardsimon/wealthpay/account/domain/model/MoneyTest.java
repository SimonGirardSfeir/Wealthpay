package org.girardsimon.wealthpay.account.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MoneyTest {

    public static Stream<Arguments> invalidMoneys() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(10L), null),
                Arguments.of(null, SupportedCurrency.USD),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidMoneys")
    void check_money_consistency(BigDecimal amount, SupportedCurrency currency) {
        // Arrange ... Act ... Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Money.of(amount, currency));
    }

}