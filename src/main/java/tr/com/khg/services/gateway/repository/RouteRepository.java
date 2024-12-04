package tr.com.khg.services.gateway.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.khg.services.gateway.entity.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {
  Optional<Route> findByRouteId(String routeId);

  List<Route> findByDeletedAtIsNull();
}
