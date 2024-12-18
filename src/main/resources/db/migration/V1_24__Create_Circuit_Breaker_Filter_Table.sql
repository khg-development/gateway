CREATE TABLE route_circuit_breaker_filter
(
    id           BIGSERIAL PRIMARY KEY,
    route_id     BIGINT,
    name         VARCHAR(255) NOT NULL,
    fallback_uri VARCHAR(255),
    status_codes VARCHAR(255),
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
