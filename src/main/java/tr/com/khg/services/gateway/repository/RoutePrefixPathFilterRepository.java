package tr.com.khg.services.gateway.repository;

import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.RoutePrefixPathFilter;
import tr.com.khg.services.gateway.repository.base.BaseRouteRepository;

@Repository
public interface RoutePrefixPathFilterRepository
    extends BaseRouteRepository<RoutePrefixPathFilter> {}
