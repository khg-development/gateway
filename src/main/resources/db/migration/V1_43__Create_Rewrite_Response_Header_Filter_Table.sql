CREATE TABLE route_rewrite_response_header_filter
(
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT,
    name        VARCHAR(255) NOT NULL,
    regexp      VARCHAR(255) NOT NULL,
    replacement VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
