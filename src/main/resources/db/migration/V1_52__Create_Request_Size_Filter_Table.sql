CREATE TABLE route_request_size_filter
(
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT,
    max_size    BIGINT NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
