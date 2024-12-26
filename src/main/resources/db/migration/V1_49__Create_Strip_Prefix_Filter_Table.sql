CREATE TABLE route_strip_prefix_filter
(
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT,
    parts       INTEGER NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
