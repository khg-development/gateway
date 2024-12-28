package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRewriteResponseHeaderFilter;
import tr.com.khg.services.gateway.model.response.RewriteResponseHeaderResponse;

@Component
public class RewriteResponseHeaderMapper
    implements BaseMapper<RouteRewriteResponseHeaderFilter, RewriteResponseHeaderResponse> {
  @Override
  public RouteRewriteResponseHeaderFilter toEntity(RewriteResponseHeaderResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RewriteResponseHeaderResponse toDto(RouteRewriteResponseHeaderFilter entity) {
    if (entity == null) {
      return null;
    }
    return RewriteResponseHeaderResponse.builder()
        .name(entity.getName())
        .regexp(entity.getRegexp())
        .replacement(entity.getReplacement())
        .build();
  }
}
