CREATE SCHEMA IF NOT EXISTS account;

CREATE TABLE IF NOT EXISTS account.event_store (
    id              BIGSERIAL PRIMARY KEY,
    account_id      UUID        NOT NULL,
    version         BIGINT      NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_event_store_account_id_version
    ON account.event_store(account_id, version);

CREATE TABLE IF NOT EXISTS account.account_balance_view (
    account_id        UUID        PRIMARY KEY,
    currency          VARCHAR(3)  NOT NULL,
    balance           NUMERIC(19,4) NOT NULL,
    reserved          NUMERIC(19,4) NOT NULL,
    status            VARCHAR(20) NOT NULL,
    version           BIGINT      NOT NULL,
    last_updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);