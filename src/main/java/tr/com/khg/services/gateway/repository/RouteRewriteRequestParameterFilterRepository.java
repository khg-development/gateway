package tr.com.khg.services.gateway.repository;

import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.RouteRewriteRequestParameterFilter;
import tr.com.khg.services.gateway.repository.base.BaseRouteRepository;

@Repository
public interface RouteRewriteRequestParameterFilterRepository
    extends BaseRouteRepository<RouteRewriteRequestParameterFilter> {}
