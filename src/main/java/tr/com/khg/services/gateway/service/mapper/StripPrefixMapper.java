package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteStripPrefixFilter;
import tr.com.khg.services.gateway.model.response.StripPrefixResponse;

@Component
public class StripPrefixMapper implements BaseMapper<RouteStripPrefixFilter, StripPrefixResponse> {
  @Override
  public RouteStripPrefixFilter toEntity(StripPrefixResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public StripPrefixResponse toDto(RouteStripPrefixFilter entity) {
    if (entity == null) {
      return null;
    }
    return StripPrefixResponse.builder().parts(entity.getParts()).build();
  }
}
