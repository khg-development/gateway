CREATE TABLE route_remote_addr_predications
(
    id       BIGSERIAL PRIMARY KEY,
    source   VARCHAR(255) NOT NULL,
    route_id BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
