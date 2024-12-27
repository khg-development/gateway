package tr.com.khg.services.gateway.repository;

import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.RouteSetRequestHostHeaderFilter;
import tr.com.khg.services.gateway.repository.base.BaseRouteRepository;

@Repository
public interface RouteSetRequestHostHeaderFilterRepository
    extends BaseRouteRepository<RouteSetRequestHostHeaderFilter> {}
