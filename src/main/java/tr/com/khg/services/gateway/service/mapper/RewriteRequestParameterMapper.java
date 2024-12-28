package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRewriteRequestParameterFilter;
import tr.com.khg.services.gateway.model.response.RewriteRequestParameterResponse;

@Component
public class RewriteRequestParameterMapper
    implements BaseMapper<RouteRewriteRequestParameterFilter, RewriteRequestParameterResponse> {
  @Override
  public RouteRewriteRequestParameterFilter toEntity(RewriteRequestParameterResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RewriteRequestParameterResponse toDto(RouteRewriteRequestParameterFilter entity) {
    if (entity == null) {
      return null;
    }
    return RewriteRequestParameterResponse.builder()
        .name(entity.getName())
        .replacement(entity.getReplacement())
        .build();
  }
}
