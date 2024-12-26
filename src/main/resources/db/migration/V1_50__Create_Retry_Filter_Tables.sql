CREATE TABLE route_retry_filter
(
    id                      BIGSERIAL PRIMARY KEY,
    route_id                BIGINT,
    retries                 INTEGER NOT NULL,
    first_backoff           BIGINT,
    max_backoff             BIGINT,
    factor                  INTEGER,
    based_on_previous_value BOOLEAN,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);

CREATE TABLE route_retry_filter_statuses
(
    retry_filter_id BIGINT,
    status          TEXT NOT NULL,
    FOREIGN KEY (retry_filter_id) REFERENCES route_retry_filter (id) ON DELETE CASCADE
);

CREATE TABLE route_retry_filter_methods
(
    retry_filter_id BIGINT,
    method          VARCHAR(255) NOT NULL,
    FOREIGN KEY (retry_filter_id) REFERENCES route_retry_filter (id) ON DELETE CASCADE
);

CREATE TABLE route_retry_filter_series
(
    retry_filter_id BIGINT,
    series          VARCHAR(255) NOT NULL,
    FOREIGN KEY (retry_filter_id) REFERENCES route_retry_filter (id) ON DELETE CASCADE
);
