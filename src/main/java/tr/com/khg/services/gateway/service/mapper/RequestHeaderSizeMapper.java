package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRequestHeaderSizeFilter;
import tr.com.khg.services.gateway.model.response.RequestHeaderSizeResponse;

@Component
public class RequestHeaderSizeMapper
    implements BaseMapper<RouteRequestHeaderSizeFilter, RequestHeaderSizeResponse> {
  @Override
  public RouteRequestHeaderSizeFilter toEntity(RequestHeaderSizeResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestHeaderSizeResponse toDto(RouteRequestHeaderSizeFilter entity) {
    if (entity == null) {
      return null;
    }
    return RequestHeaderSizeResponse.builder()
        .maxSize(entity.getMaxSize())
        .errorHeaderName(entity.getErrorHeaderName())
        .build();
  }
}
