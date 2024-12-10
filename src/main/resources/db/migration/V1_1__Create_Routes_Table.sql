CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    route_id VARCHAR(255) NOT NULL UNIQUE,
    route_definition JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
); 