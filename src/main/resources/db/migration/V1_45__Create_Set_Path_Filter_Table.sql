CREATE TABLE route_set_path_filter
(
    id       BIGSERIAL PRIMARY KEY,
    route_id BIGINT,
    template VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
