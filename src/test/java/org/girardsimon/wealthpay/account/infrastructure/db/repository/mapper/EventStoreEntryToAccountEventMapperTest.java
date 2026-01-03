package org.girardsimon.wealthpay.account.infrastructure.db.repository.mapper;

import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.event.AccountOpened;
import org.girardsimon.wealthpay.account.domain.event.ReservationCaptured;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.domain.model.ReservationId;
import org.girardsimon.wealthpay.account.domain.model.SupportedCurrency;
import org.girardsimon.wealthpay.account.jooq.tables.pojos.EventStore;
import org.jooq.JSONB;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventStoreEntryToAccountEventMapperTest {

    EventStoreEntryToAccountEventMapper mapper = new EventStoreEntryToAccountEventMapper(new ObjectMapper());

    public static Stream<Arguments> eventSourceAndExpectedEvent() {
        EventStore accountOpenedEvent = new EventStore();
        accountOpenedEvent.setEventType("AccountOpened");
        UUID accountId = UUID.randomUUID();
        accountOpenedEvent.setAccountId(accountId);
        accountOpenedEvent.setVersion(1L);
        accountOpenedEvent.setPayload(JSONB.valueOf("""
                {
                    "currency": "USD",
                    "initialBalance": 10.00,
                    "occurredAt": "2025-11-16T15:00:00Z"
                }
                """));
        AccountOpened accountOpened = new AccountOpened(
                AccountId.of(accountId),
                Instant.parse("2025-11-16T15:00:00Z"),
                1L,
                SupportedCurrency.USD,
                Money.of(BigDecimal.valueOf(10.00), SupportedCurrency.USD)
        );
        EventStore reservationCapturedEvent = new EventStore();
        reservationCapturedEvent.setEventType("ReservationCaptured");
        reservationCapturedEvent.setAccountId(accountId);
        reservationCapturedEvent.setVersion(4L);
        reservationCapturedEvent.setPayload(JSONB.valueOf("""
                {
                    "reservationId": "09518c66-ff5e-4596-9049-74dfbdf6f6db",
                    "currency": "EUR",
                    "amount": 40.00,
                    "occurredAt": "2025-11-16T15:00:00Z"
                }
                """));
        ReservationCaptured reservationCaptured = new ReservationCaptured(
                AccountId.of(accountId),
                ReservationId.of(UUID.fromString("09518c66-ff5e-4596-9049-74dfbdf6f6db")),
                Money.of(BigDecimal.valueOf(40.00), SupportedCurrency.EUR),
                4L,
                Instant.parse("2025-11-16T15:00:00Z")
        );
        return Stream.of(
                Arguments.of(accountOpenedEvent, accountOpened),
                Arguments.of(reservationCapturedEvent, reservationCaptured)
        );
    }

    @ParameterizedTest
    @MethodSource("eventSourceAndExpectedEvent")
    void deserialize_account_event(EventStore eventStore, AccountEvent expectedEvent) {
        // Act
        AccountEvent deserializedEvent = mapper.apply(eventStore);

        // Assert
        assertThat(deserializedEvent).isEqualTo(expectedEvent);
    }

}