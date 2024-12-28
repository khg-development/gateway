package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteLocalResponseCacheFilter;
import tr.com.khg.services.gateway.model.response.LocalResponseCacheResponse;

@Component
public class LocalResponseCacheMapper
    implements BaseMapper<RouteLocalResponseCacheFilter, LocalResponseCacheResponse> {
  @Override
  public RouteLocalResponseCacheFilter toEntity(LocalResponseCacheResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalResponseCacheResponse toDto(RouteLocalResponseCacheFilter entity) {
    if (entity == null) {
      return null;
    }
    return LocalResponseCacheResponse.builder()
        .size(entity.getSize())
        .timeToLive(entity.getTimeToLive())
        .build();
  }
}
