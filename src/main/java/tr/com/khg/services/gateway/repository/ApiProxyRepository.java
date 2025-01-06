package tr.com.khg.services.gateway.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.khg.services.gateway.entity.ApiProxy;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ApiProxyRepository extends JpaRepository<ApiProxy, Long> {
  Optional<ApiProxy> findByName(String name);

  @Modifying
  @Transactional
  void deleteByName(String name);
}
