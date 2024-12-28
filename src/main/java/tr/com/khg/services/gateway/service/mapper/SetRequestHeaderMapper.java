package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteSetRequestHeaderFilter;
import tr.com.khg.services.gateway.model.response.SetRequestHeaderResponse;

@Component
public class SetRequestHeaderMapper
    implements BaseMapper<RouteSetRequestHeaderFilter, SetRequestHeaderResponse> {
  @Override
  public RouteSetRequestHeaderFilter toEntity(SetRequestHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SetRequestHeaderResponse toDto(RouteSetRequestHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return SetRequestHeaderResponse.builder()
        .name(entity.getName())
        .value(entity.getValue())
        .build();
  }
}
