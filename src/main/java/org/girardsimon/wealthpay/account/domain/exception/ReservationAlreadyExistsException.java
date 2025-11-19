package org.girardsimon.wealthpay.account.domain.exception;

import org.girardsimon.wealthpay.account.domain.model.ReservationId;

public class ReservationAlreadyExistsException extends RuntimeException {
    public ReservationAlreadyExistsException(ReservationId reservationId) {
        super("Reservation already exists: " + reservationId);
    }
}
