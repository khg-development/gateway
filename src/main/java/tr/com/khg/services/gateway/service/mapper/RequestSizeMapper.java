package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRequestSizeFilter;
import tr.com.khg.services.gateway.model.response.RequestSizeResponse;

@Component
public class RequestSizeMapper implements BaseMapper<RouteRequestSizeFilter, RequestSizeResponse> {
  @Override
  public RouteRequestSizeFilter toEntity(RequestSizeResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestSizeResponse toDto(RouteRequestSizeFilter entity) {
    if (entity == null) {
      return null;
    }
    return RequestSizeResponse.builder().maxSize(entity.getMaxSize()).build();
  }
}
