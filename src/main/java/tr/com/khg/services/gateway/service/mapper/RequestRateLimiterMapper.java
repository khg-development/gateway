package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRequestRateLimiterFilter;
import tr.com.khg.services.gateway.model.response.RequestRateLimiterResponse;

@Component
public class RequestRateLimiterMapper
    implements BaseMapper<RouteRequestRateLimiterFilter, RequestRateLimiterResponse> {
  @Override
  public RouteRequestRateLimiterFilter toEntity(RequestRateLimiterResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RequestRateLimiterResponse toDto(RouteRequestRateLimiterFilter entity) {
    if (entity == null) {
      return null;
    }
    return RequestRateLimiterResponse.builder()
        .replenishRate(entity.getReplenishRate())
        .burstCapacity(entity.getBurstCapacity())
        .requestedTokens(entity.getRequestedTokens())
        .keyResolver(entity.getKeyResolver())
        .headerName(entity.getHeaderName())
        .build();
  }
}
