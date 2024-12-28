package tr.com.khg.services.gateway.service.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.model.response.FiltersResponse;

@Component
@RequiredArgsConstructor
public class FiltersResponseMapper implements BaseMapper<Route, FiltersResponse> {
  private final AddRequestHeaderFilterMapper addRequestHeaderFilterMapper;
  private final AddRequestHeaderIfNotPresentFilterMapper addRequestHeaderIfNotPresentFilterMapper;
  private final AddRequestParameterFilterMapper addRequestParameterFilterMapper;
  private final AddResponseHeaderFilterMapper addResponseHeaderFilterMapper;
  private final CircuitBreakerMapper circuitBreakerMapper;
  private final DedupeResponseHeaderMapper dedupeResponseHeaderMapper;
  private final FallbackHeadersMapper fallbackHeadersMapper;
  private final LocalResponseCacheMapper localResponseCacheMapper;
  private final MapRequestHeaderMapper mapRequestHeaderMapper;
  private final PrefixPathMapper prefixPathMapper;
  private final RedirectToMapper redirectToMapper;
  private final RemoveJsonAttributesResponseBodyMapper removeJsonAttributesResponseBodyMapper;
  private final RemoveRequestHeaderMapper removeRequestHeaderMapper;
  private final RemoveRequestParameterMapper removeRequestParameterMapper;
  private final RemoveResponseHeaderMapper removeResponseHeaderMapper;
  private final RequestHeaderSizeMapper requestHeaderSizeMapper;
  private final RequestRateLimiterMapper requestRateLimiterMapper;
  private final RewriteLocationResponseHeaderMapper rewriteLocationResponseHeaderMapper;
  private final RewritePathMapper rewritePathMapper;
  private final RewriteRequestParameterMapper rewriteRequestParameterMapper;
  private final RewriteResponseHeaderMapper rewriteResponseHeaderMapper;
  private final SetPathMapper setPathMapper;
  private final SetRequestHeaderMapper setRequestHeaderMapper;
  private final SetResponseHeaderMapper setResponseHeaderMapper;
  private final SetStatusMapper setStatusMapper;
  private final StripPrefixMapper stripPrefixMapper;
  private final RetryMapper retryMapper;
  private final RequestSizeMapper requestSizeMapper;
  private final SetRequestHostHeaderMapper setRequestHostHeaderMapper;

  @Override
  public Route toEntity(FiltersResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FiltersResponse toDto(Route entity) {
    if (entity == null) {
      return null;
    }
    return FiltersResponse.builder()
        .addRequestHeaders(
            addRequestHeaderFilterMapper.toDtoList(entity.getRouteAddRequestHeaderFilters()))
        .addRequestHeadersIfNotPresent(
            addRequestHeaderIfNotPresentFilterMapper.toDtoList(
                entity.getRouteAddRequestHeaderIfNotPresentFilters()))
        .addRequestParameters(
            addRequestParameterFilterMapper.toDtoList(entity.getRouteAddRequestParameterFilters()))
        .addResponseHeaders(
            addResponseHeaderFilterMapper.toDtoList(entity.getRouteAddResponseHeaderFilters()))
        .circuitBreakers(circuitBreakerMapper.toDtoList(entity.getRouteCircuitBreakerFilters()))
        .dedupeResponseHeaders(
            dedupeResponseHeaderMapper.toDtoList(entity.getRouteDedupeResponseHeaderFilters()))
        .fallbackHeaders(fallbackHeadersMapper.toDtoList(entity.getRouteFallbackHeadersFilters()))
        .localResponseCache(
            localResponseCacheMapper.toDtoList(entity.getRouteLocalResponseCacheFilters()))
        .mapRequestHeaders(
            mapRequestHeaderMapper.toDtoList(entity.getRouteMapRequestHeaderFilters()))
        .prefixPaths(prefixPathMapper.toDtoList(entity.getRoutePrefixPathFilters()))
        .redirects(redirectToMapper.toDtoList(entity.getRouteRedirectToFilters()))
        .removeJsonAttributesResponseBody(
            removeJsonAttributesResponseBodyMapper.toDtoList(
                entity.getRouteRemoveJsonAttributesResponseBodyFilters()))
        .removeRequestHeaders(
            removeRequestHeaderMapper.toDtoList(entity.getRouteRemoveRequestHeaderFilters()))
        .removeRequestParameters(
            removeRequestParameterMapper.toDtoList(entity.getRouteRemoveRequestParameterFilters()))
        .removeResponseHeaders(
            removeResponseHeaderMapper.toDtoList(entity.getRouteRemoveResponseHeaderFilters()))
        .requestHeaderSizes(
            requestHeaderSizeMapper.toDtoList(entity.getRouteRequestHeaderSizeFilters()))
        .requestRateLimiter(
            requestRateLimiterMapper.toDto(entity.getRouteRequestRateLimiterFilter()))
        .rewriteLocationResponseHeaders(
            rewriteLocationResponseHeaderMapper.toDtoList(
                entity.getRouteRewriteLocationResponseHeaderFilters()))
        .rewritePaths(rewritePathMapper.toDtoList(entity.getRouteRewritePathFilters()))
        .rewriteRequestParameters(
            rewriteRequestParameterMapper.toDtoList(
                entity.getRouteRewriteRequestParameterFilters()))
        .rewriteResponseHeaders(
            rewriteResponseHeaderMapper.toDtoList(entity.getRouteRewriteResponseHeaderFilters()))
        .setPath(setPathMapper.toDto(entity.getRouteSetPathFilter()))
        .setRequestHeaders(
            setRequestHeaderMapper.toDtoList(entity.getRouteSetRequestHeaderFilters()))
        .setResponseHeaders(
            setResponseHeaderMapper.toDtoList(entity.getRouteSetResponseHeaderFilters()))
        .setStatus(setStatusMapper.toDto(entity.getRouteSetStatusFilter()))
        .stripPrefix(stripPrefixMapper.toDto(entity.getRouteStripPrefixFilter()))
        .retry(retryMapper.toDto(entity.getRouteRetryFilter()))
        .requestSize(requestSizeMapper.toDto(entity.getRouteRequestSizeFilter()))
        .setRequestHostHeader(
            setRequestHostHeaderMapper.toDto(entity.getRouteSetRequestHostHeaderFilter()))
        .build();
  }
}
