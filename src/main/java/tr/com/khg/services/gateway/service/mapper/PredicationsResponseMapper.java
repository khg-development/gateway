package tr.com.khg.services.gateway.service.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.model.response.PredicationsResponse;

@Component
@RequiredArgsConstructor
public class PredicationsResponseMapper implements BaseMapper<Route, PredicationsResponse> {
  private final CookiePredicationMapper cookiePredicationMapper;
  private final HeaderPredicationMapper headerPredicationMapper;
  private final HostPredicationMapper hostPredicationMapper;
  private final QueryPredicationMapper queryPredicationMapper;
  private final RemoteAddrPredicationMapper remoteAddrPredicationMapper;
  private final WeightPredicationMapper weightPredicationMapper;
  private final XForwardedRemoteAddrPredicationMapper xForwardedRemoteAddrPredicationMapper;

  @Override
  public Route toEntity(PredicationsResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PredicationsResponse toDto(Route entity) {
    if (entity == null) {
      return null;
    }
    return PredicationsResponse.builder()
        .cookies(cookiePredicationMapper.toDtoList(entity.getRouteCookiePredications()))
        .headers(headerPredicationMapper.toDtoList(entity.getRouteHeaderPredications()))
        .hosts(hostPredicationMapper.toDtoList(entity.getRouteHostPredications()))
        .queries(queryPredicationMapper.toDtoList(entity.getRouteQueryPredications()))
        .remoteAddresses(
            remoteAddrPredicationMapper.toDtoList(entity.getRouteRemoteAddrPredications()))
        .weights(weightPredicationMapper.toDtoList(entity.getRouteWeightPredications()))
        .xforwardedRemoteAddresses(
            xForwardedRemoteAddrPredicationMapper.toDtoList(
                entity.getRouteXForwardedRemoteAddrPredications()))
        .build();
  }
}
