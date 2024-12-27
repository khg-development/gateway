package tr.com.khg.services.gateway.repository;

import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.RouteRequestHeaderSizeFilter;
import tr.com.khg.services.gateway.repository.base.BaseRouteRepository;

@Repository
public interface RouteRequestHeaderSizeFilterRepository
    extends BaseRouteRepository<RouteRequestHeaderSizeFilter> {}
