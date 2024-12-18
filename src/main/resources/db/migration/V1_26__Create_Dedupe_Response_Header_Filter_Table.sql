CREATE TABLE route_dedupe_response_header_filter
(
    id       BIGSERIAL PRIMARY KEY,
    route_id BIGINT,
    name     VARCHAR(255) NOT NULL,
    strategy VARCHAR(20)  NOT NULL DEFAULT 'RETAIN_FIRST',
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
