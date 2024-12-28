package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteFallbackHeadersFilter;
import tr.com.khg.services.gateway.model.response.FallbackHeadersResponse;

@Component
public class FallbackHeadersMapper
    implements BaseMapper<RouteFallbackHeadersFilter, FallbackHeadersResponse> {
  @Override
  public RouteFallbackHeadersFilter toEntity(FallbackHeadersResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FallbackHeadersResponse toDto(RouteFallbackHeadersFilter entity) {
    if (entity == null) {
      return null;
    }
    return FallbackHeadersResponse.builder()
        .executionExceptionTypeHeaderName(entity.getExecutionExceptionTypeHeaderName())
        .executionExceptionMessageHeaderName(entity.getExecutionExceptionMessageHeaderName())
        .rootCauseExceptionTypeHeaderName(entity.getRootCauseExceptionTypeHeaderName())
        .rootCauseExceptionMessageHeaderName(entity.getRootCauseExceptionMessageHeaderName())
        .build();
  }
}
