package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteAddResponseHeaderFilter;
import tr.com.khg.services.gateway.model.response.AddResponseHeaderFilterResponse;

@Component
public class AddResponseHeaderFilterMapper
    implements BaseMapper<RouteAddResponseHeaderFilter, AddResponseHeaderFilterResponse> {
  @Override
  public RouteAddResponseHeaderFilter toEntity(AddResponseHeaderFilterResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AddResponseHeaderFilterResponse toDto(RouteAddResponseHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return AddResponseHeaderFilterResponse.builder()
        .name(entity.getName())
        .value(entity.getValue())
        .build();
  }
}
