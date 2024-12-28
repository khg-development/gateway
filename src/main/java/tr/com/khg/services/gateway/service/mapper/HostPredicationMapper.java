package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteHostPredication;
import tr.com.khg.services.gateway.model.response.HostPredicationResponse;

@Component
public class HostPredicationMapper
    implements BaseMapper<RouteHostPredication, HostPredicationResponse> {
  @Override
  public RouteHostPredication toEntity(HostPredicationResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public HostPredicationResponse toDto(RouteHostPredication entity) {
    if (entity == null) {
      return null;
    }
    return HostPredicationResponse.builder().pattern(entity.getPattern()).build();
  }
}
