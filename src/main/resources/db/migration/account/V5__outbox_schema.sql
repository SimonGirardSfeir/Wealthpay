create table outbox
(
    outbox_id         bigserial primary key,
    event_id          uuid        not null unique,
    aggregate_type    text        not null,
    aggregate_id      uuid        not null,
    aggregate_version bigint      not null,
    event_type        text        not null,
    occurred_at       timestamptz not null,
    payload           jsonb       not null,

    status            text        not null default 'PENDING', -- PENDING, PUBLISHED, FAILED
    publish_attempts  int         not null default 0,
    last_error        text        null,
    available_at      timestamptz not null default now(),
    published_at      timestamptz null
);

create index outbox_pending_idx
    on outbox (status, available_at, outbox_id);

create index outbox_aggregate_order_idx
    on outbox (aggregate_id, aggregate_version);
