ALTER TABLE account.event_store
    ADD COLUMN event_id UUID NOT NULL DEFAULT gen_random_uuid();

ALTER TABLE account.event_store
    ALTER COLUMN event_id DROP DEFAULT;

CREATE UNIQUE INDEX idx_event_store_event_id ON account.event_store (event_id);
