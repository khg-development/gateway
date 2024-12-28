package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteSetResponseHeaderFilter;
import tr.com.khg.services.gateway.model.response.SetResponseHeaderResponse;

@Component
public class SetResponseHeaderMapper
    implements BaseMapper<RouteSetResponseHeaderFilter, SetResponseHeaderResponse> {
  @Override
  public RouteSetResponseHeaderFilter toEntity(SetResponseHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SetResponseHeaderResponse toDto(RouteSetResponseHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return SetResponseHeaderResponse.builder()
        .name(entity.getName())
        .value(entity.getValue())
        .build();
  }
}
