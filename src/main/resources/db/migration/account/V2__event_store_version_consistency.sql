ALTER TABLE account.event_store
    ADD CONSTRAINT uq_event_store_account_version
        UNIQUE (account_id, version);