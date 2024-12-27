package tr.com.khg.services.gateway.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import tr.com.khg.services.gateway.entity.Route;

@NoRepositoryBean
public interface BaseRouteRepository<T> extends JpaRepository<T, Long> {
  @Modifying
  @Transactional
  @Query("DELETE FROM #{#entityName} e WHERE e.route = :route")
  void deleteByRoute(Route route);
}
