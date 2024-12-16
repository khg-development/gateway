CREATE TABLE route_add_request_header_filters
(
    id            BIGSERIAL PRIMARY KEY,
    header_name   VARCHAR(255) NOT NULL,
    header_value  VARCHAR(255) NOT NULL,
    route_id      BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
