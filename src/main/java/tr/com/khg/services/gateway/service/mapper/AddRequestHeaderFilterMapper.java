package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteAddRequestHeaderFilter;
import tr.com.khg.services.gateway.model.response.AddRequestHeaderFilterResponse;

@Component
public class AddRequestHeaderFilterMapper
    implements BaseMapper<RouteAddRequestHeaderFilter, AddRequestHeaderFilterResponse> {
  @Override
  public RouteAddRequestHeaderFilter toEntity(AddRequestHeaderFilterResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AddRequestHeaderFilterResponse toDto(RouteAddRequestHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return AddRequestHeaderFilterResponse.builder()
        .name(entity.getName())
        .value(entity.getValue())
        .build();
  }
}
