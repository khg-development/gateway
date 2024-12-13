CREATE TABLE route_query_predications
(
    id               BIGSERIAL PRIMARY KEY,
    param_name       VARCHAR(255) NOT NULL,
    param_value_regexp VARCHAR(255),
    route_id         BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
