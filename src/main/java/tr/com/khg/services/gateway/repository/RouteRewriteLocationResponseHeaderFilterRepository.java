package tr.com.khg.services.gateway.repository;

import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.RouteRewriteLocationResponseHeaderFilter;
import tr.com.khg.services.gateway.repository.base.BaseRouteRepository;

@Repository
public interface RouteRewriteLocationResponseHeaderFilterRepository
    extends BaseRouteRepository<RouteRewriteLocationResponseHeaderFilter> {}
