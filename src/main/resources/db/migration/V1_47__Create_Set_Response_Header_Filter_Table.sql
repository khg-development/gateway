CREATE TABLE route_set_response_header_filter
(
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT,
    name        VARCHAR(255) NOT NULL,
    value       VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
