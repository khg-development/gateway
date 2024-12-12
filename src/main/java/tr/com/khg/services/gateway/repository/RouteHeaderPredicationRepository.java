package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteHeaderPredication;

public interface RouteHeaderPredicationRepository
    extends JpaRepository<RouteHeaderPredication, Long> {
  void deleteByRoute(Route route);
}
