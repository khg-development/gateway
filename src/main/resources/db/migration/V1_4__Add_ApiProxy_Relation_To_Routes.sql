ALTER TABLE routes
    ADD COLUMN api_proxy_id BIGINT NOT NULL;
ALTER TABLE routes
    ADD CONSTRAINT fk_route_api_proxy
        FOREIGN KEY (api_proxy_id) REFERENCES api_proxies (id);
