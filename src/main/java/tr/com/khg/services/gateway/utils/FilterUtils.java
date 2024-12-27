package tr.com.khg.services.gateway.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.*;
import tr.com.khg.services.gateway.model.request.Filters;
import tr.com.khg.services.gateway.model.request.RequestRateLimiterRequest;

@Component
@RequiredArgsConstructor
public class FilterUtils {

  public void setAllFilters(Filters f, Route route) {
    route.setRouteAddRequestHeaderFilters(createAddRequestHeaderFilters(f, route));
    route.setRouteAddRequestHeaderIfNotPresentFilters(
        createAddRequestHeaderIfNotPresentFilters(f, route));
    route.setRouteAddRequestParameterFilters(createAddRequestParameterFilters(f, route));
    route.setRouteAddResponseHeaderFilters(createAddResponseHeaderFilters(f, route));
    route.setRouteCircuitBreakerFilters(createCircuitBreakerFilters(f, route));
    route.setRouteDedupeResponseHeaderFilters(createDedupeResponseHeaderFilters(f, route));
    route.setRouteFallbackHeadersFilters(createFallbackHeadersFilters(f, route));
    route.setRouteLocalResponseCacheFilters(createLocalResponseCacheFilters(f, route));
    route.setRouteMapRequestHeaderFilters(createMapRequestHeaderFilters(f, route));
    route.setRoutePrefixPathFilters(createPrefixPathFilters(f, route));
    route.setRouteRedirectToFilters(createRedirectToFilters(f, route));
    route.setRouteRemoveJsonAttributesResponseBodyFilters(
        createRemoveJsonAttributesResponseBodyFilters(f, route));
    route.setRouteRemoveRequestHeaderFilters(createRemoveRequestHeaderFilters(f, route));
    route.setRouteRemoveRequestParameterFilters(createRemoveRequestParameterFilters(f, route));
    route.setRouteRemoveResponseHeaderFilters(createRemoveResponseHeaderFilters(f, route));
    route.setRouteRequestHeaderSizeFilters(createRequestHeaderSizeFilters(f, route));
    route.setRouteRequestRateLimiterFilter(createRequestRateLimiterFilter(f, route));
    route.setRouteRewriteLocationResponseHeaderFilters(
        createRewriteLocationResponseHeaderFilters(f, route));
    route.setRouteRewritePathFilters(createRewritePathFilters(f, route));
    route.setRouteRewriteRequestParameterFilters(createRewriteRequestParameterFilters(f, route));
    route.setRouteRewriteResponseHeaderFilters(createRewriteResponseHeaderFilters(f, route));
    route.setRouteSetPathFilter(createSetPathFilter(f, route));
    route.setRouteSetRequestHeaderFilters(createSetRequestHeaderFilters(f, route));
    route.setRouteSetResponseHeaderFilters(createSetResponseHeaderFilters(f, route));
    route.setRouteSetStatusFilter(createSetStatusFilter(f, route));
    route.setRouteStripPrefixFilter(createStripPrefixFilter(f, route));
    route.setRouteRetryFilter(createRetryFilter(f, route));
    route.setRouteRequestSizeFilter(createRequestSizeFilter(f, route));
    route.setRouteSetRequestHostHeaderFilter(createSetRequestHostHeaderFilter(f, route));
  }

  public List<RouteAddRequestHeaderFilter> createAddRequestHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getAddRequestHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getAddRequestHeaders().stream()
        .map(
            filter ->
                RouteAddRequestHeaderFilter.builder()
                    .name(filter.getName())
                    .value(filter.getValue())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteAddRequestHeaderIfNotPresentFilter> createAddRequestHeaderIfNotPresentFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getAddRequestHeadersIfNotPresent() == null) {
      return new ArrayList<>();
    }

    return filters.getAddRequestHeadersIfNotPresent().stream()
        .map(
            filter ->
                RouteAddRequestHeaderIfNotPresentFilter.builder()
                    .name(filter.getName())
                    .value(filter.getValue())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteAddRequestParameterFilter> createAddRequestParameterFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getAddRequestParameters() == null) {
      return new ArrayList<>();
    }

    return filters.getAddRequestParameters().stream()
        .map(
            filter ->
                RouteAddRequestParameterFilter.builder()
                    .name(filter.getName())
                    .value(filter.getValue())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteAddResponseHeaderFilter> createAddResponseHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getAddResponseHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getAddResponseHeaders().stream()
        .map(
            filter ->
                RouteAddResponseHeaderFilter.builder()
                    .name(filter.getName())
                    .value(filter.getValue())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteCircuitBreakerFilter> createCircuitBreakerFilters(Filters filters, Route route) {
    if (filters == null || filters.getCircuitBreakers() == null) {
      return new ArrayList<>();
    }

    return filters.getCircuitBreakers().stream()
        .map(
            filter ->
                RouteCircuitBreakerFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .fallbackUri(filter.getFallbackUri())
                    .statusCodes(
                        filter.getStatusCodes() != null
                            ? String.join(",", filter.getStatusCodes())
                            : null)
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteDedupeResponseHeaderFilter> createDedupeResponseHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getDedupeResponseHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getDedupeResponseHeaders().stream()
        .map(
            filter ->
                RouteDedupeResponseHeaderFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .strategy(filter.getStrategy())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteFallbackHeadersFilter> createFallbackHeadersFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getFallbackHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getFallbackHeaders().stream()
        .map(
            filter ->
                RouteFallbackHeadersFilter.builder()
                    .route(route)
                    .executionExceptionTypeHeaderName(filter.getExecutionExceptionTypeHeaderName())
                    .executionExceptionMessageHeaderName(
                        filter.getExecutionExceptionMessageHeaderName())
                    .rootCauseExceptionTypeHeaderName(filter.getRootCauseExceptionTypeHeaderName())
                    .rootCauseExceptionMessageHeaderName(
                        filter.getRootCauseExceptionMessageHeaderName())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteLocalResponseCacheFilter> createLocalResponseCacheFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getLocalResponseCache() == null) {
      return new ArrayList<>();
    }

    return filters.getLocalResponseCache().stream()
        .map(
            filter ->
                RouteLocalResponseCacheFilter.builder()
                    .route(route)
                    .size(filter.getSize())
                    .timeToLive(filter.getTimeToLive())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteMapRequestHeaderFilter> createMapRequestHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getMapRequestHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getMapRequestHeaders().stream()
        .map(
            filter ->
                RouteMapRequestHeaderFilter.builder()
                    .route(route)
                    .fromHeader(filter.getFromHeader())
                    .toHeader(filter.getToHeader())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RoutePrefixPathFilter> createPrefixPathFilters(Filters filters, Route route) {
    if (filters == null || filters.getPrefixPaths() == null) {
      return new ArrayList<>();
    }

    return filters.getPrefixPaths().stream()
        .map(
            filter ->
                RoutePrefixPathFilter.builder().route(route).prefix(filter.getPrefix()).build())
        .collect(Collectors.toList());
  }

  public List<RouteRedirectToFilter> createRedirectToFilters(Filters filters, Route route) {
    if (filters == null || filters.getRedirects() == null) {
      return new ArrayList<>();
    }

    return filters.getRedirects().stream()
        .map(
            filter ->
                RouteRedirectToFilter.builder()
                    .route(route)
                    .status(filter.getStatus())
                    .url(filter.getUrl())
                    .includeRequestParams(filter.isIncludeRequestParams())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRemoveJsonAttributesResponseBodyFilter>
      createRemoveJsonAttributesResponseBodyFilters(Filters filters, Route route) {
    if (filters == null || filters.getRemoveJsonAttributesResponseBody() == null) {
      return new ArrayList<>();
    }

    return filters.getRemoveJsonAttributesResponseBody().stream()
        .map(
            filter ->
                RouteRemoveJsonAttributesResponseBodyFilter.builder()
                    .route(route)
                    .attributes(String.join(",", filter.getAttributes()))
                    .recursive(filter.isRecursive())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRemoveRequestHeaderFilter> createRemoveRequestHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getRemoveRequestHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getRemoveRequestHeaders().stream()
        .map(
            filter ->
                RouteRemoveRequestHeaderFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRemoveRequestParameterFilter> createRemoveRequestParameterFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getRemoveRequestParameters() == null) {
      return new ArrayList<>();
    }

    return filters.getRemoveRequestParameters().stream()
        .map(
            filter ->
                RouteRemoveRequestParameterFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRemoveResponseHeaderFilter> createRemoveResponseHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getRemoveResponseHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getRemoveResponseHeaders().stream()
        .map(
            filter ->
                RouteRemoveResponseHeaderFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRequestHeaderSizeFilter> createRequestHeaderSizeFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getRequestHeaderSizes() == null) {
      return new ArrayList<>();
    }

    return filters.getRequestHeaderSizes().stream()
        .map(
            filter ->
                RouteRequestHeaderSizeFilter.builder()
                    .route(route)
                    .maxSize(filter.getMaxSize())
                    .errorHeaderName(filter.getErrorHeaderName())
                    .build())
        .collect(Collectors.toList());
  }

  public RouteRequestRateLimiterFilter createRequestRateLimiterFilter(
      Filters filters, Route route) {
    if (filters == null || filters.getRequestRateLimiter() == null) {
      return null;
    }

    RequestRateLimiterRequest filter = filters.getRequestRateLimiter();
    return RouteRequestRateLimiterFilter.builder()
        .route(route)
        .replenishRate(filter.getReplenishRate())
        .burstCapacity(filter.getBurstCapacity())
        .requestedTokens(filter.getRequestedTokens())
        .keyResolver(filter.getKeyResolver())
        .headerName(filter.getHeaderName())
        .build();
  }

  public List<RouteRewriteLocationResponseHeaderFilter> createRewriteLocationResponseHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getRewriteLocationResponseHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getRewriteLocationResponseHeaders().stream()
        .map(
            filter ->
                RouteRewriteLocationResponseHeaderFilter.builder()
                    .route(route)
                    .stripVersionMode(filter.getStripVersionMode())
                    .locationHeaderName(filter.getLocationHeaderName())
                    .hostValue(filter.getHostValue())
                    .protocolsRegex(filter.getProtocolsRegex())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRewritePathFilter> createRewritePathFilters(Filters filters, Route route) {
    if (filters == null || filters.getRewritePaths() == null) {
      return new ArrayList<>();
    }

    return filters.getRewritePaths().stream()
        .map(
            filter ->
                RouteRewritePathFilter.builder()
                    .route(route)
                    .regexp(filter.getRegexp())
                    .replacement(filter.getReplacement())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRewriteRequestParameterFilter> createRewriteRequestParameterFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getRewriteRequestParameters() == null) {
      return new ArrayList<>();
    }

    return filters.getRewriteRequestParameters().stream()
        .map(
            filter ->
                RouteRewriteRequestParameterFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .replacement(filter.getReplacement())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteRewriteResponseHeaderFilter> createRewriteResponseHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getRewriteResponseHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getRewriteResponseHeaders().stream()
        .map(
            filter ->
                RouteRewriteResponseHeaderFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .regexp(filter.getRegexp())
                    .replacement(filter.getReplacement())
                    .build())
        .collect(Collectors.toList());
  }

  public RouteSetPathFilter createSetPathFilter(Filters filters, Route route) {
    if (filters == null || filters.getSetPath() == null) {
      return null;
    }

    return RouteSetPathFilter.builder()
        .route(route)
        .template(filters.getSetPath().getTemplate())
        .build();
  }

  public List<RouteSetRequestHeaderFilter> createSetRequestHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getSetRequestHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getSetRequestHeaders().stream()
        .map(
            filter ->
                RouteSetRequestHeaderFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .value(filter.getValue())
                    .build())
        .collect(Collectors.toList());
  }

  public List<RouteSetResponseHeaderFilter> createSetResponseHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getSetResponseHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getSetResponseHeaders().stream()
        .map(
            filter ->
                RouteSetResponseHeaderFilter.builder()
                    .route(route)
                    .name(filter.getName())
                    .value(filter.getValue())
                    .build())
        .collect(Collectors.toList());
  }

  public RouteSetStatusFilter createSetStatusFilter(Filters filters, Route route) {
    if (filters == null || filters.getSetStatus() == null) {
      return null;
    }

    return RouteSetStatusFilter.builder()
        .route(route)
        .status(filters.getSetStatus().getStatus())
        .build();
  }

  public RouteStripPrefixFilter createStripPrefixFilter(Filters filters, Route route) {
    if (filters == null || filters.getStripPrefix() == null) {
      return null;
    }

    return RouteStripPrefixFilter.builder()
        .route(route)
        .parts(filters.getStripPrefix().getParts())
        .build();
  }

  public RouteRetryFilter createRetryFilter(Filters filters, Route route) {
    if (filters == null || filters.getRetry() == null) {
      return null;
    }

    return RouteRetryFilter.builder()
        .route(route)
        .retries(filters.getRetry().getRetries())
        .statuses(filters.getRetry().getStatuses().stream().map(Enum::name).toList())
        .methods(filters.getRetry().getMethods().stream().map(HttpMethod::name).toList())
        .series(filters.getRetry().getSeries().stream().map(Enum::name).toList())
        .firstBackoff(filters.getRetry().getFirstBackoff())
        .maxBackoff(filters.getRetry().getMaxBackoff())
        .factor(filters.getRetry().getFactor())
        .basedOnPreviousValue(filters.getRetry().getBasedOnPreviousValue())
        .build();
  }

  public RouteRequestSizeFilter createRequestSizeFilter(Filters filters, Route route) {
    if (filters == null || filters.getRequestSize() == null) {
      return null;
    }

    return RouteRequestSizeFilter.builder()
        .route(route)
        .maxSize(filters.getRequestSize().getMaxSize())
        .build();
  }

  public RouteSetRequestHostHeaderFilter createSetRequestHostHeaderFilter(
      Filters filters, Route route) {
    if (filters == null || filters.getSetRequestHostHeader() == null) {
      return null;
    }

    return RouteSetRequestHostHeaderFilter.builder()
        .route(route)
        .host(filters.getSetRequestHostHeader().getHost())
        .build();
  }
}
