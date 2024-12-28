package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteSetRequestHostHeaderFilter;
import tr.com.khg.services.gateway.model.response.SetRequestHostHeaderResponse;

@Component
public class SetRequestHostHeaderMapper
    implements BaseMapper<RouteSetRequestHostHeaderFilter, SetRequestHostHeaderResponse> {
  @Override
  public RouteSetRequestHostHeaderFilter toEntity(SetRequestHostHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SetRequestHostHeaderResponse toDto(RouteSetRequestHostHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return SetRequestHostHeaderResponse.builder().host(entity.getHost()).build();
  }
}
