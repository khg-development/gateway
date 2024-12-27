CREATE TABLE route_set_request_host_header_filter
(
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT,
    host        VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
