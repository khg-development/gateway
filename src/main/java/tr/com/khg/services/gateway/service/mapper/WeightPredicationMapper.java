package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteWeightPredication;
import tr.com.khg.services.gateway.model.response.WeightPredicationResponse;

@Component
public class WeightPredicationMapper
    implements BaseMapper<RouteWeightPredication, WeightPredicationResponse> {
  @Override
  public RouteWeightPredication toEntity(WeightPredicationResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public WeightPredicationResponse toDto(RouteWeightPredication entity) {
    if (entity == null) {
      return null;
    }
    return WeightPredicationResponse.builder()
        .group(entity.getGroup())
        .weight(entity.getWeight())
        .build();
  }
}
