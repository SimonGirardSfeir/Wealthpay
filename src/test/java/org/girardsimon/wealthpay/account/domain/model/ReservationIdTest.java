package org.girardsimon.wealthpay.account.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ReservationIdTest {

    @Test
    void check_reservation_id_consistency() {
        // Arrange ... Act ... Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new ReservationId(null));
    }

}