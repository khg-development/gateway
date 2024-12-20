CREATE TABLE route_local_response_cache_filter
(
    id                BIGSERIAL PRIMARY KEY,
    route_id          BIGINT,
    size              VARCHAR(255),
    time_to_live      VARCHAR(255),
    no_cache_strategy VARCHAR(255) DEFAULT 'SKIP_UPDATE_CACHE_ENTRY',
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
