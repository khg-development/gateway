package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteSetPathFilter;
import tr.com.khg.services.gateway.model.response.SetPathResponse;

@Component
public class SetPathMapper implements BaseMapper<RouteSetPathFilter, SetPathResponse> {
  @Override
  public RouteSetPathFilter toEntity(SetPathResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SetPathResponse toDto(RouteSetPathFilter entity) {
    if (entity == null) {
      return null;
    }
    return SetPathResponse.builder().template(entity.getTemplate()).build();
  }
}
