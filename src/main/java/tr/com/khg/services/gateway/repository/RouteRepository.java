package tr.com.khg.services.gateway.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

public interface RouteRepository extends JpaRepository<Route, Long> {

  List<Route> findByApiProxyNameOrderById(@Param("proxyName") String proxyName);

  Optional<Route> findByApiProxyAndPathAndMethod(
      ApiProxy apiProxy, String path, HttpMethods method);

  Optional<Route> findByApiProxyNameAndRouteId(
      @Param("proxyName") String proxyName, @Param("routeId") String routeId);

  List<Route> findByEnabled(@Param("enabled") boolean enabled);
}
