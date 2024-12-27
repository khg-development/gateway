package tr.com.khg.services.gateway.repository;

import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.RouteRetryFilter;
import tr.com.khg.services.gateway.repository.base.BaseRouteRepository;

@Repository
public interface RouteRetryFilterRepository extends BaseRouteRepository<RouteRetryFilter> {}
