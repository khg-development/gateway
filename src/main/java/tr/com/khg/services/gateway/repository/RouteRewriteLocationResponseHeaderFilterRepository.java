package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteRewriteLocationResponseHeaderFilter;

@Repository
public interface RouteRewriteLocationResponseHeaderFilterRepository
    extends JpaRepository<RouteRewriteLocationResponseHeaderFilter, Long> {

  @Modifying
  @Transactional
  @Query("DELETE FROM RouteRewriteLocationResponseHeaderFilter f WHERE f.route = :route")
  void deleteByRoute(Route route);
}
