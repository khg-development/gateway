package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteHeaderPredication;
import tr.com.khg.services.gateway.model.response.HeaderPredicationResponse;

@Component
public class HeaderPredicationMapper
    implements BaseMapper<RouteHeaderPredication, HeaderPredicationResponse> {
  @Override
  public RouteHeaderPredication toEntity(HeaderPredicationResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public HeaderPredicationResponse toDto(RouteHeaderPredication entity) {
    if (entity == null) {
      return null;
    }
    return HeaderPredicationResponse.builder()
        .name(entity.getName())
        .regexp(entity.getRegexp())
        .build();
  }
}
