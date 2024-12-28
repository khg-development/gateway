package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteRedirectToFilter;
import tr.com.khg.services.gateway.model.response.RedirectToResponse;

@Component
public class RedirectToMapper implements BaseMapper<RouteRedirectToFilter, RedirectToResponse> {
  @Override
  public RouteRedirectToFilter toEntity(RedirectToResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RedirectToResponse toDto(RouteRedirectToFilter entity) {
    if (entity == null) {
      return null;
    }
    return RedirectToResponse.builder()
        .status(entity.getStatus())
        .url(entity.getUrl())
        .includeRequestParams(entity.isIncludeRequestParams())
        .build();
  }
}
