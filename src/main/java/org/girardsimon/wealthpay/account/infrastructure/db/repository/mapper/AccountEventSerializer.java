package org.girardsimon.wealthpay.account.infrastructure.db.repository.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.girardsimon.wealthpay.account.domain.event.AccountClosed;
import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.event.AccountOpened;
import org.girardsimon.wealthpay.account.domain.event.FundsCredited;
import org.girardsimon.wealthpay.account.domain.event.FundsDebited;
import org.girardsimon.wealthpay.account.domain.event.FundsReserved;
import org.girardsimon.wealthpay.account.domain.event.ReservationCancelled;
import org.jooq.JSONB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class AccountEventSerializer implements Function<AccountEvent, JSONB> {

    private static final Logger log = LoggerFactory.getLogger(AccountEventSerializer.class);

    private final ObjectMapper objectMapper;

    public AccountEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JSONB apply(AccountEvent accountEvent) {
        return switch (accountEvent) {
            case AccountClosed accountClosed -> null;
            case AccountOpened accountOpened -> mapAccountOpenedPayload(accountOpened);
            case FundsCredited fundsCredited -> null;
            case FundsDebited fundsDebited -> null;
            case FundsReserved fundsReserved -> null;
            case ReservationCancelled reservationCancelled -> null;
        };
    }

    private JSONB mapAccountOpenedPayload(AccountOpened accountOpened) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("currency", accountOpened.currency().getCurrencyCode());
        root.put("initialBalance", accountOpened.initialBalance().amount());
        root.put("occurredAt", accountOpened.occurredAt().toString());
        try {
            String jsonString = objectMapper.writeValueAsString(root);
            return JSONB.valueOf(jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing event payload", e);
            throw new IllegalStateException("Error while parsing event payload");
        }
    }
}
