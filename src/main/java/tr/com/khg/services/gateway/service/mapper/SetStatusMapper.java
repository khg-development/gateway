package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteSetStatusFilter;
import tr.com.khg.services.gateway.model.response.SetStatusResponse;

@Component
public class SetStatusMapper implements BaseMapper<RouteSetStatusFilter, SetStatusResponse> {
  @Override
  public RouteSetStatusFilter toEntity(SetStatusResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SetStatusResponse toDto(RouteSetStatusFilter entity) {
    if (entity == null) {
      return null;
    }
    return SetStatusResponse.builder().status(entity.getStatus()).build();
  }
}
