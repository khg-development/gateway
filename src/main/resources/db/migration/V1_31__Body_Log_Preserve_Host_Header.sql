ALTER TABLE routes
    ADD COLUMN body_log_enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN preserve_host_header BOOLEAN NOT NULL DEFAULT FALSE;
