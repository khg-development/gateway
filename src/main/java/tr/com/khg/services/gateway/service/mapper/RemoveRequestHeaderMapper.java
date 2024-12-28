package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRemoveRequestHeaderFilter;
import tr.com.khg.services.gateway.model.response.RemoveRequestHeaderResponse;

@Component
public class RemoveRequestHeaderMapper
    implements BaseMapper<RouteRemoveRequestHeaderFilter, RemoveRequestHeaderResponse> {

  @Override
  public RouteRemoveRequestHeaderFilter toEntity(RemoveRequestHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RemoveRequestHeaderResponse toDto(RouteRemoveRequestHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return RemoveRequestHeaderResponse.builder().name(entity.getName()).build();
  }
}
