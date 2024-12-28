package tr.com.khg.services.gateway.service.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.model.response.RouteResponse;

@Component
@RequiredArgsConstructor
public class RouteMapper implements BaseMapper<Route, RouteResponse> {
  private final FiltersResponseMapper filtersResponseMapper;
  private final PredicationsResponseMapper predicationsResponseMapper;

  @Override
  public Route toEntity(RouteResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RouteResponse toDto(Route entity) {
    if (entity == null) {
      return null;
    }
    return RouteResponse.builder()
        .routeId(entity.getRouteId())
        .enabled(entity.isEnabled())
        .path(entity.getPath())
        .method(entity.getMethod())
        .matchTrailingSlash(entity.isMatchTrailingSlash())
        .activationTime(entity.getActivationTime())
        .expirationTime(entity.getExpirationTime())
        .bodyLogEnabled(entity.isBodyLogEnabled())
        .preserveHostHeader(entity.isPreserveHostHeader())
        .secureHeadersEnabled(entity.isSecureHeadersEnabled())
        .saveSessionEnabled(entity.isSaveSessionEnabled())
        .filters(filtersResponseMapper.toDto(entity))
        .predications(predicationsResponseMapper.toDto(entity))
        .build();
  }
}
