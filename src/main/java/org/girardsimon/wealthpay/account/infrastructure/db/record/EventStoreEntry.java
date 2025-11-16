package org.girardsimon.wealthpay.account.infrastructure.db.record;

import org.jooq.JSONB;

import java.time.Instant;
import java.util.UUID;

public record EventStoreEntry(
        Long id,
        UUID accountId,
        long version,
        String eventType,
        JSONB payload,
        Instant createdAt
) {
}
