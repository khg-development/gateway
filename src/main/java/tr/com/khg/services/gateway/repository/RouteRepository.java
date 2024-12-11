package tr.com.khg.services.gateway.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

public interface RouteRepository extends JpaRepository<Route, Long> {
  @Query(
      """
      SELECT DISTINCT r FROM Route r
      LEFT JOIN FETCH r.routeHeaderConfigurations
      WHERE r.apiProxy.name = :proxyName
      ORDER BY r.id
      """)
  List<Route> findByApiProxyNameOrderById(@Param("proxyName") String proxyName);

  Optional<Route> findByApiProxyAndPathAndMethod(
      ApiProxy apiProxy, String path, HttpMethods method);

  @Query(
      "SELECT r FROM Route r LEFT JOIN FETCH r.routeHeaderConfigurations WHERE r.apiProxy.name = :proxyName AND r.routeId = :routeId")
  Optional<Route> findByApiProxyNameAndRouteId(
      @Param("proxyName") String proxyName, @Param("routeId") String routeId);

  @Query(
      "SELECT r FROM Route r LEFT JOIN FETCH r.routeHeaderConfigurations WHERE r.enabled = :enabled")
  List<Route> findByEnabled(@Param("enabled") boolean enabled);
}
