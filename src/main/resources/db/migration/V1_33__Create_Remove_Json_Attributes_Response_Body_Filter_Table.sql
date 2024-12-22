CREATE TABLE route_remove_json_attributes_response_body_filter
(
    id         BIGSERIAL PRIMARY KEY,
    route_id   BIGINT,
    attributes TEXT    NOT NULL,
    recursive  BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
