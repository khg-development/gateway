package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteRemoveJsonAttributesResponseBodyFilter;

@Repository
public interface RouteRemoveJsonAttributesResponseBodyFilterRepository
    extends JpaRepository<RouteRemoveJsonAttributesResponseBodyFilter, Long> {

  @Modifying
  @Transactional
  @Query("DELETE FROM RouteRemoveJsonAttributesResponseBodyFilter f WHERE f.route = :route")
  void deleteByRoute(Route route);
}
