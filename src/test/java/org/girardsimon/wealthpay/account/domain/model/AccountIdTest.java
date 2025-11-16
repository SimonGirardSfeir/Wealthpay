package org.girardsimon.wealthpay.account.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountIdTest {

    @Test
    void check_account_id_consistency() {
        // Arrange ... Act ... Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new AccountId(null));
    }

}