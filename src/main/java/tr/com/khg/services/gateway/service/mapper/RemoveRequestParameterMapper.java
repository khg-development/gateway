package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRemoveRequestParameterFilter;
import tr.com.khg.services.gateway.model.response.RemoveRequestParameterResponse;

@Component
public class RemoveRequestParameterMapper
    implements BaseMapper<RouteRemoveRequestParameterFilter, RemoveRequestParameterResponse> {
  @Override
  public RouteRemoveRequestParameterFilter toEntity(RemoveRequestParameterResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RemoveRequestParameterResponse toDto(RouteRemoveRequestParameterFilter entity) {
    if (entity == null) {
      return null;
    }
    return RemoveRequestParameterResponse.builder().name(entity.getName()).build();
  }
}
