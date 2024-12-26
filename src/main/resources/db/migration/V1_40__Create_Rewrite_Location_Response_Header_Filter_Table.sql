CREATE TABLE route_rewrite_location_response_header_filter
(
    id                   BIGSERIAL PRIMARY KEY,
    route_id             BIGINT,
    strip_version_mode   VARCHAR(20)  NOT NULL DEFAULT 'AS_IN_REQUEST',
    location_header_name VARCHAR(255) NOT NULL DEFAULT 'Location',
    host_value           VARCHAR(255),
    protocols_regex      VARCHAR(255) NOT NULL DEFAULT 'https?|ftps?',
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
