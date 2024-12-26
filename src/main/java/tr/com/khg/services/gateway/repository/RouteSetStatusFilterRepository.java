package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteSetStatusFilter;

@Repository
public interface RouteSetStatusFilterRepository
    extends JpaRepository<RouteSetStatusFilter, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM RouteSetStatusFilter f WHERE f.route = :route")
    void deleteByRoute(Route route);
}
