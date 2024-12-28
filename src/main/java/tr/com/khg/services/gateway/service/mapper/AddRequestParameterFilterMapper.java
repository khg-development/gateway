package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteAddRequestParameterFilter;
import tr.com.khg.services.gateway.model.response.AddRequestParameterFilterResponse;

@Component
public class AddRequestParameterFilterMapper
    implements BaseMapper<RouteAddRequestParameterFilter, AddRequestParameterFilterResponse> {
  @Override
  public RouteAddRequestParameterFilter toEntity(AddRequestParameterFilterResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AddRequestParameterFilterResponse toDto(RouteAddRequestParameterFilter entity) {
    if (entity == null) {
      return null;
    }
    return AddRequestParameterFilterResponse.builder()
        .name(entity.getName())
        .value(entity.getValue())
        .build();
  }
}
