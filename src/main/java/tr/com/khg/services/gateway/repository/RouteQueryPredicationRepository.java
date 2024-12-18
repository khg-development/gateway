package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteQueryPredication;

public interface RouteQueryPredicationRepository
    extends JpaRepository<RouteQueryPredication, Long> {
  @Modifying
  @Transactional
  @Query("DELETE FROM RouteQueryPredication q WHERE q.route = :route")
  void deleteByRoute(Route route);
}
