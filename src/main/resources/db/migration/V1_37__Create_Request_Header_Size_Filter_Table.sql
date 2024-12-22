CREATE TABLE route_request_header_size_filter
(
    id                BIGSERIAL PRIMARY KEY,
    route_id          BIGINT,
    max_size          VARCHAR(255) NOT NULL,
    error_header_name VARCHAR(255) NOT NULL DEFAULT 'errorMessage',
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
