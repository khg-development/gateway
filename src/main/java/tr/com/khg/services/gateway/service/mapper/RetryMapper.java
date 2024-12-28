package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRetryFilter;
import tr.com.khg.services.gateway.model.response.RetryResponse;

@Component
public class RetryMapper implements BaseMapper<RouteRetryFilter, RetryResponse> {
  @Override
  public RouteRetryFilter toEntity(RetryResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RetryResponse toDto(RouteRetryFilter entity) {
    if (entity == null) {
      return null;
    }
    return RetryResponse.builder()
        .retries(entity.getRetries())
        .statuses(entity.getStatuses())
        .methods(entity.getMethods())
        .series(entity.getSeries())
        .firstBackoff(entity.getFirstBackoff())
        .maxBackoff(entity.getMaxBackoff())
        .factor(entity.getFactor())
        .basedOnPreviousValue(entity.getBasedOnPreviousValue())
        .build();
  }
}
