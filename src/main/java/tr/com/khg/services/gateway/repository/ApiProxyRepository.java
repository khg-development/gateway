package tr.com.khg.services.gateway.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.khg.services.gateway.entity.ApiProxy;

public interface ApiProxyRepository extends JpaRepository<ApiProxy, Long> {
  Optional<ApiProxy> findByName(String name);
  Page<ApiProxy> findAll(Pageable pageable);
}
