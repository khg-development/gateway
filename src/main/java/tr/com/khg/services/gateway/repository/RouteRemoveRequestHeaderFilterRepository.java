package tr.com.khg.services.gateway.repository;

import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.RouteRemoveRequestHeaderFilter;
import tr.com.khg.services.gateway.repository.base.BaseRouteRepository;

@Repository
public interface RouteRemoveRequestHeaderFilterRepository
    extends BaseRouteRepository<RouteRemoveRequestHeaderFilter> {}
