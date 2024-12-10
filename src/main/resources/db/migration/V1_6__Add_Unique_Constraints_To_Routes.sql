ALTER TABLE routes
    ADD CONSTRAINT uk_route_id_api_proxy_id UNIQUE (route_id, api_proxy_id),
    ADD CONSTRAINT uk_api_proxy_path_method UNIQUE (api_proxy_id, path, method);
