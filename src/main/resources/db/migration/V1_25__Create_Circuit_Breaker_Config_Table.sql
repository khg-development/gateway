CREATE TABLE circuit_breaker_config
(
    id                                                  BIGSERIAL PRIMARY KEY,
    name                                                VARCHAR(255) NOT NULL UNIQUE,
    failure_rate_threshold                              INTEGER      NOT NULL DEFAULT 50,
    slow_call_rate_threshold                            INTEGER      NOT NULL DEFAULT 100,
    slow_call_duration_threshold                        BIGINT       NOT NULL DEFAULT 60000,
    permitted_number_of_calls_in_half_open_state        INTEGER      NOT NULL DEFAULT 10,
    sliding_window_size                                 INTEGER      NOT NULL DEFAULT 100,
    minimum_number_of_calls                             INTEGER      NOT NULL DEFAULT 10,
    wait_duration_in_open_state                         BIGINT       NOT NULL DEFAULT 60000,
    automatic_transition_from_open_to_half_open_enabled BOOLEAN      NOT NULL DEFAULT false,
    sliding_window_type                                 VARCHAR(20)  NOT NULL DEFAULT 'COUNT_BASED',
    ignore_exceptions                                   TEXT,
    record_exceptions                                   TEXT,
    max_wait_duration_in_half_open_state                BIGINT       NOT NULL DEFAULT 0,
    created_at                                          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                                          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
