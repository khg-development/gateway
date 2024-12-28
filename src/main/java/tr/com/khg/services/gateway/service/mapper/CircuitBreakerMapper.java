package tr.com.khg.services.gateway.service.mapper;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteCircuitBreakerFilter;
import tr.com.khg.services.gateway.model.response.CircuitBreakerResponse;

@Component
public class CircuitBreakerMapper
    implements BaseMapper<RouteCircuitBreakerFilter, CircuitBreakerResponse> {
  @Override
  public RouteCircuitBreakerFilter toEntity(CircuitBreakerResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CircuitBreakerResponse toDto(RouteCircuitBreakerFilter entity) {
    if (entity == null) {
      return null;
    }
    return CircuitBreakerResponse.builder()
        .name(entity.getName())
        .fallbackUri(entity.getFallbackUri())
        .statusCodes(
            entity.getStatusCodes() != null
                ? Arrays.asList(entity.getStatusCodes().split(","))
                : null)
        .build();
  }
}
