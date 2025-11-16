package org.girardsimon.wealthpay.account.infrastructure.db.repository;

import org.girardsimon.wealthpay.account.application.AccountEventStore;
import org.girardsimon.wealthpay.account.domain.event.AccountEvent;
import org.girardsimon.wealthpay.account.domain.model.AccountId;
import org.girardsimon.wealthpay.account.infrastructure.db.record.EventStoreEntry;
import org.girardsimon.wealthpay.account.infrastructure.db.repository.mapper.AccountEventSerializer;
import org.girardsimon.wealthpay.account.infrastructure.db.repository.mapper.EventStoreEntryToAccountEventMapper;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@Repository
public class AccountEventRepository implements AccountEventStore {

    private final DSLContext dslContext;
    private final EventStoreEntryToAccountEventMapper eventStoreEntryToAccountEventMapper;
    private final AccountEventSerializer accountEventSerializer;

    public AccountEventRepository(DSLContext dslContext, EventStoreEntryToAccountEventMapper eventStoreEntryToAccountEventMapper, AccountEventSerializer accountEventSerializer) {
        this.dslContext = dslContext;
        this.eventStoreEntryToAccountEventMapper = eventStoreEntryToAccountEventMapper;
        this.accountEventSerializer = accountEventSerializer;
    }

    @Override
    public List<AccountEvent> loadEvents(AccountId accountId) {
        UUID accountUuid = accountId.id();

        List<EventStoreEntry> rows = dslContext.select(
                        field("id", Long.class),
                        field("account_id", UUID.class),
                        field("version", Long.class),
                        field("event_type", String.class),
                        field("payload", JSONB.class),
                        field("created_at", Instant.class)
                )
                .from(table(name("account", "event_store")))
                .where(field("account_id", UUID.class).eq(accountUuid))
                .orderBy(field("version").asc())
                .fetchInto(EventStoreEntry.class);

        return rows.stream()
                .map(eventStoreEntryToAccountEventMapper)
                .toList();
    }

    @Override
    public void appendEvents(AccountId accountId, long expectedVersion, List<AccountEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        UUID accountUuid = accountId.id();

        Long currentVersion = dslContext
                .select(max(field("version", Long.class)))
                .from(table(name("account", "event_store")))
                .where(field("account_id", UUID.class).eq(accountUuid))
                .fetchOneInto(Long.class);

        long actualVersion = currentVersion != null ? currentVersion : 0L;

        if (actualVersion != expectedVersion) {
            throw new OptimisticLockingFailureException(
                    "Version mismatch for account %s: expected %d but found %d"
                            .formatted(accountUuid, expectedVersion, actualVersion)
            );
        }

        try {
            for (AccountEvent event : events) {
                String eventType = event.getClass().getSimpleName();
                JSONB payload = accountEventSerializer.apply(event);

                dslContext.insertInto(table(name("account", "event_store")))
                        .columns(
                                field("account_id"),
                                field("version"),
                                field("event_type"),
                                field("payload")
                        )
                        .values(
                                accountUuid,
                                event.version(),
                                eventType,
                                payload
                        )
                        .execute();
            }
        } catch (DataIntegrityViolationException e) {
            throw new OptimisticLockingFailureException(
                    "Concurrent modification detected for account %s".formatted(accountUuid), e);
        }
    }


}
