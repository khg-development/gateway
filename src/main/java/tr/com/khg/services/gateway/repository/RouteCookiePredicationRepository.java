package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteCookiePredication;

public interface RouteCookiePredicationRepository
    extends JpaRepository<RouteCookiePredication, Long> {
  @Modifying
  @Transactional
  @Query("DELETE FROM RouteCookiePredication c WHERE c.route = :route")
  void deleteByRoute(Route route);
}
