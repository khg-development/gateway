package tr.com.khg.services.gateway.utils;

import java.util.ArrayList;
import java.util.List;
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
    if (filters == null || filters.getCircuitBreaker() == null) {
      return new ArrayList<>();
    }

    return filters.getCircuitBreaker().stream()
        .map(
            filter ->
                RouteCircuitBreakerFilter.builder()
                    .name(filter.getName())
                    .fallbackUri(filter.getFallbackUri())
                    .route(route)
                    .build())
        .toList();
  }
}
