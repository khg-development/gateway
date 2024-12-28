package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRemoveResponseHeaderFilter;
import tr.com.khg.services.gateway.model.response.RemoveResponseHeaderResponse;

@Component
public class RemoveResponseHeaderMapper
    implements BaseMapper<RouteRemoveResponseHeaderFilter, RemoveResponseHeaderResponse> {
  @Override
  public RouteRemoveResponseHeaderFilter toEntity(RemoveResponseHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RemoveResponseHeaderResponse toDto(RouteRemoveResponseHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return RemoveResponseHeaderResponse.builder().name(entity.getName()).build();
  }
}
