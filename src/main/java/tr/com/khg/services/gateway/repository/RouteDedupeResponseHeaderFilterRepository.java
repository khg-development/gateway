package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteDedupeResponseHeaderFilter;

@Repository
public interface RouteDedupeResponseHeaderFilterRepository
    extends JpaRepository<RouteDedupeResponseHeaderFilter, Long> {

  @Modifying
  @Transactional
  @Query("DELETE FROM RouteDedupeResponseHeaderFilter f WHERE f.route = :route")
  void deleteByRoute(Route route);
}
