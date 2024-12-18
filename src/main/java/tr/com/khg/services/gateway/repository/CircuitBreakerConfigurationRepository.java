package tr.com.khg.services.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.com.khg.services.gateway.entity.CircuitBreakerConfiguration;
import java.util.Optional;

@Repository
public interface CircuitBreakerConfigurationRepository extends JpaRepository<CircuitBreakerConfiguration, Long> {
  Optional<CircuitBreakerConfiguration> findByName(String name);

  boolean existsByName(String name);
}
