CREATE TABLE route_weight_predications
(
    id         BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(255) NOT NULL,
    weight     INTEGER      NOT NULL,
    route_id   BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
