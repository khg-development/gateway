CREATE TABLE route_add_request_parameter_filters
(
    id              BIGSERIAL PRIMARY KEY,
    parameter_name  VARCHAR(255) NOT NULL,
    parameter_value VARCHAR(255) NOT NULL,
    route_id        BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
