CREATE TABLE route_set_status_filter
(
    id       BIGSERIAL PRIMARY KEY,
    route_id BIGINT,
    status   VARCHAR(50) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
