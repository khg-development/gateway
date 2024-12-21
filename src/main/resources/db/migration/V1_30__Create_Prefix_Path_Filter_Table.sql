CREATE TABLE route_prefix_path_filter
(
    id        BIGSERIAL PRIMARY KEY,
    route_id  BIGINT,
    prefix    VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
