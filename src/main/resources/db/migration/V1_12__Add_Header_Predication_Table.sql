CREATE TABLE route_header_predications
(
    id                  BIGSERIAL PRIMARY KEY,
    header_name         VARCHAR(255) NOT NULL,
    header_value_regexp VARCHAR(255) NOT NULL,
    route_id            BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id)
);
