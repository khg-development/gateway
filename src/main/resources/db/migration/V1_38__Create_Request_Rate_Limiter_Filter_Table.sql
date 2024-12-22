CREATE TABLE route_request_rate_limiter_filter
(
    id               BIGSERIAL PRIMARY KEY,
    route_id         BIGINT UNIQUE,
    replenish_rate   INTEGER      NOT NULL,
    burst_capacity   INTEGER      NOT NULL,
    requested_tokens INTEGER      NOT NULL DEFAULT 1,
    key_resolver     VARCHAR(255) NOT NULL DEFAULT 'PRINCIPAL_NAME',
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
