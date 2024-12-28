package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteMapRequestHeaderFilter;
import tr.com.khg.services.gateway.model.response.MapRequestHeaderResponse;

@Component
public class MapRequestHeaderMapper
    implements BaseMapper<RouteMapRequestHeaderFilter, MapRequestHeaderResponse> {
  @Override
  public RouteMapRequestHeaderFilter toEntity(MapRequestHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MapRequestHeaderResponse toDto(RouteMapRequestHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return MapRequestHeaderResponse.builder()
        .fromHeader(entity.getFromHeader())
        .toHeader(entity.getToHeader())
        .build();
  }
}
