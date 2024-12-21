CREATE TABLE route_redirect_to_filter
(
    id                     BIGSERIAL PRIMARY KEY,
    route_id               BIGINT,
    status                 INTEGER      NOT NULL,
    url                    VARCHAR(255) NOT NULL,
    include_request_params BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
