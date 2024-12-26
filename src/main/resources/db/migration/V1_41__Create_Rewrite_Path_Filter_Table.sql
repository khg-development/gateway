CREATE TABLE route_rewrite_path_filter
(
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT,
    regexp      VARCHAR(255) NOT NULL,
    replacement VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
