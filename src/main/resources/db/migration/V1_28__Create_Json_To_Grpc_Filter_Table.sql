CREATE TABLE route_json_to_grpc_filter
(
    id               BIGSERIAL PRIMARY KEY,
    route_id         BIGINT,
    proto_descriptor VARCHAR(255) NOT NULL,
    proto_file       VARCHAR(255) NOT NULL,
    service_name     VARCHAR(255) NOT NULL,
    method_name      VARCHAR(255) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
