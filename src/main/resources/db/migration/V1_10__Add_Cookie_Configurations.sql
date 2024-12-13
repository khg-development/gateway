CREATE TABLE route_cookie_configurations (
    id BIGSERIAL PRIMARY KEY,
    cookie_name VARCHAR(255) NOT NULL,
    cookie_value_regexp VARCHAR(255) NOT NULL,
    route_id BIGINT,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE
);
