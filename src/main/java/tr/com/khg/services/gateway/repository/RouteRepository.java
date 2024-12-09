package tr.com.khg.services.gateway.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

public interface RouteRepository extends JpaRepository<Route, Long> {
  Optional<Route> findByRouteId(String routeId);

  List<Route> findByEnabled(boolean enabled);

  List<Route> findByApiProxyName(String proxyName);

  Optional<Route> findByRouteIdAndApiProxy(String routeId, ApiProxy apiProxy);

  Optional<Route> findByApiProxyAndPathAndMethod(
      ApiProxy apiProxy, String path, HttpMethods method);
}
