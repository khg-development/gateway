package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteDedupeResponseHeaderFilter;
import tr.com.khg.services.gateway.model.response.DedupeResponseHeaderResponse;

@Component
public class DedupeResponseHeaderMapper
    implements BaseMapper<RouteDedupeResponseHeaderFilter, DedupeResponseHeaderResponse> {
  @Override
  public RouteDedupeResponseHeaderFilter toEntity(DedupeResponseHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DedupeResponseHeaderResponse toDto(RouteDedupeResponseHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return DedupeResponseHeaderResponse.builder()
        .name(entity.getName())
        .strategy(entity.getStrategy())
        .build();
  }
}
