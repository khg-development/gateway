package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteAddRequestHeaderIfNotPresentFilter;
import tr.com.khg.services.gateway.model.response.AddRequestHeaderIfNotPresentFilterResponse;

@Component
public class AddRequestHeaderIfNotPresentFilterMapper
    implements BaseMapper<
        RouteAddRequestHeaderIfNotPresentFilter, AddRequestHeaderIfNotPresentFilterResponse> {
  @Override
  public RouteAddRequestHeaderIfNotPresentFilter toEntity(
      AddRequestHeaderIfNotPresentFilterResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AddRequestHeaderIfNotPresentFilterResponse toDto(
      RouteAddRequestHeaderIfNotPresentFilter entity) {
    if (entity == null) {
      return null;
    }
    return AddRequestHeaderIfNotPresentFilterResponse.builder()
        .name(entity.getName())
        .value(entity.getValue())
        .build();
  }
}
