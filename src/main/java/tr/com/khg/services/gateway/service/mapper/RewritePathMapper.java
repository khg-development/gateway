package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRewritePathFilter;
import tr.com.khg.services.gateway.model.response.RewritePathResponse;

@Component
public class RewritePathMapper implements BaseMapper<RouteRewritePathFilter, RewritePathResponse> {
  @Override
  public RouteRewritePathFilter toEntity(RewritePathResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RewritePathResponse toDto(RouteRewritePathFilter entity) {
    if (entity == null) {
      return null;
    }
    return RewritePathResponse.builder()
        .regexp(entity.getRegexp())
        .replacement(entity.getReplacement())
        .build();
  }
}
