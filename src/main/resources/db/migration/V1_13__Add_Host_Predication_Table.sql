CREATE TABLE route_host_predications
(
    id           BIGSERIAL PRIMARY KEY,
    host_pattern VARCHAR(255) NOT NULL,
    route_id     BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
