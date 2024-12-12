package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteHeaderPredication;

public interface RouteHeaderPredicationRepository
    extends JpaRepository<RouteHeaderPredication, Long> {
  @Transactional
  void deleteByRoute(Route route);
}
