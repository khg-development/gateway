package tr.com.khg.services.gateway.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.*;
import tr.com.khg.services.gateway.model.request.Filters;

@Component
@RequiredArgsConstructor
public class FilterUtils {

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

  public List<RouteJsonToGrpcFilter> createJsonToGrpcFilters(Filters filters, Route route) {
    if (filters == null || filters.getJsonToGrpc() == null) {
      return new ArrayList<>();
    }

    return filters.getJsonToGrpc().stream()
        .map(
            filter ->
                RouteJsonToGrpcFilter.builder()
                    .route(route)
                    .protoDescriptor(filter.getProtoDescriptor())
                    .protoFile(filter.getProtoFile())
                    .serviceName(filter.getServiceName())
                    .methodName(filter.getMethodName())
                    .build())
        .collect(Collectors.toList());
  }
}
