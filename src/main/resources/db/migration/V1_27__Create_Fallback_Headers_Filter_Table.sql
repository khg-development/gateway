CREATE TABLE route_fallback_headers_filter
(
    id                                       BIGSERIAL PRIMARY KEY,
    route_id                                 BIGINT,
    execution_exception_type_header_name     VARCHAR(255),
    execution_exception_message_header_name  VARCHAR(255),
    root_cause_exception_type_header_name    VARCHAR(255),
    root_cause_exception_message_header_name VARCHAR(255),
    FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE
);
