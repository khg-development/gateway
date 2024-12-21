CREATE TABLE route_map_request_header_filter
(
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT,
    from_header VARCHAR(255) NOT NULL,
    to_header   VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
