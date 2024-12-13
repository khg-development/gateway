CREATE TABLE route_header_configurations
(
    id           BIGSERIAL PRIMARY KEY,
    header_key   VARCHAR(255) NOT NULL,
    header_value VARCHAR(255) NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    route_id     BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
