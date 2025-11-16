package org.girardsimon.wealthpay.account.infrastructure.db.repository.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.event.AccountOpened;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.domain.model.Money;
import org.girardsimon.wealthpay.account.infrastructure.db.record.EventStoreEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.function.Function;

@Component
public class EventStoreEntryToAccountEventMapper implements Function<EventStoreEntry, AccountEvent> {

    private static final Logger log = LoggerFactory.getLogger(EventStoreEntryToAccountEventMapper.class);

    private final ObjectMapper objectMapper;

    public EventStoreEntryToAccountEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public AccountEvent apply(EventStoreEntry eventStoreEntry) {
        String eventType = eventStoreEntry.eventType();

        return switch (eventType) {
            case "AccountOpened" -> mapAccountOpened(eventStoreEntry);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

    private AccountOpened mapAccountOpened(EventStoreEntry eventStoreEntry) {
        try {
            JsonNode root = objectMapper.readTree(eventStoreEntry.payload().data());

            Currency currency = Currency.getInstance(root.get("currency").asText());
            BigDecimal amount = root.get("initialBalance").decimalValue();

            return new AccountOpened(
                    AccountId.of(eventStoreEntry.accountId()),
                    Instant.parse(root.get("occurredAt").asText()),
                    eventStoreEntry.version(),
                    currency,
                    Money.of(amount, currency)
            );
        } catch (JsonProcessingException e) {
            log.error("Error while parsing event payload", e);
            throw new IllegalStateException("Error while parsing event payload");
        }
    }
}
