package tr.com.khg.services.gateway.utils;

import static tr.com.khg.services.gateway.entity.enums.FilterType.*;
import static tr.com.khg.services.gateway.entity.enums.PredicateType.*;
import static tr.com.khg.services.gateway.entity.enums.PredicateType.X_FORWARDED_REMOTE_ADDR;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.enums.DedupeStrategy;
import tr.com.khg.services.gateway.entity.enums.KeyResolvers;
import tr.com.khg.services.gateway.model.request.*;

@Service
@RequiredArgsConstructor
public class RouteDefinitionService {
  private final DefinitionUtils definitionUtils;

  public RouteDefinition createRouteDefinition(RouteRequest request, ApiProxy apiProxy) {
    RouteDefinition routeDefinition = new RouteDefinition();
    routeDefinition.setId(request.getRouteId());
    routeDefinition.setUri(URI.create(apiProxy.getUri()));
    List<PredicateDefinition> predicates = new ArrayList<>();
    List<FilterDefinition> filters = new ArrayList<>();

    if (request.getBodyLogEnabled()) {
      filters.add(definitionUtils.createFilterDefinition(CACHE_REQUEST_BODY, "java.lang.String"));
    }

    if (request.getPreserveHostHeader()) {
      filters.add(definitionUtils.createFilterDefinition(PRESERVE_HOST_HEADER));
    }

    if (request.getSecureHeadersEnabled()) {
      filters.add(definitionUtils.createFilterDefinition(SECURE_HEADERS));
    }

    boolean matchTrailingSlash =
        request.getMatchTrailingSlash() != null ? request.getMatchTrailingSlash() : true;
    predicates.add(
        definitionUtils.createPredicateDefinition(
            PATH, request.getPath(), String.valueOf(matchTrailingSlash)));
    predicates.add(definitionUtils.createPredicateDefinition(METHOD, request.getMethod().name()));

    if (request.getActivationTime() != null && request.getExpirationTime() != null) {
      predicates.add(
          definitionUtils.createPredicateDefinition(
              BETWEEN, request.getActivationTime(), request.getExpirationTime()));
    }

    if (request.getActivationTime() != null && request.getExpirationTime() == null) {
      predicates.add(definitionUtils.createPredicateDefinition(AFTER, request.getActivationTime()));
    }

    if (request.getExpirationTime() != null && request.getActivationTime() == null) {
      predicates.add(
          definitionUtils.createPredicateDefinition(BEFORE, request.getExpirationTime()));
    }

    if (request.getPredications().getCookies() != null
        && !request.getPredications().getCookies().isEmpty()) {
      request
          .getPredications()
          .getCookies()
          .forEach(
              cookiePredication ->
                  predicates.add(
                      definitionUtils.createPredicateDefinition(
                          COOKIE, cookiePredication.getName(), cookiePredication.getRegexp())));
    }

    if (request.getPredications().getHeaders() != null
        && !request.getPredications().getHeaders().isEmpty()) {
      request
          .getPredications()
          .getHeaders()
          .forEach(
              headerPredication ->
                  predicates.add(
                      definitionUtils.createPredicateDefinition(
                          HEADER, headerPredication.getName(), headerPredication.getRegexp())));
    }

    if (request.getPredications().getHosts() != null
        && !request.getPredications().getHosts().isEmpty()) {
      String[] hostPatterns =
          request.getPredications().getHosts().stream()
              .map(HostPredication::getPattern)
              .toArray(String[]::new);
      predicates.add(definitionUtils.createPredicateDefinition(HOST, hostPatterns));
    }

    if (request.getPredications().getQueries() != null
        && !request.getPredications().getQueries().isEmpty()) {
      request
          .getPredications()
          .getQueries()
          .forEach(
              queryPredication -> {
                String regexp =
                    queryPredication.getRegexp() != null
                        ? queryPredication.getRegexp()
                        : RouteConstants.DEFAULT_REGEX;
                PredicateDefinition queryPredicate =
                    definitionUtils.createPredicateDefinition(
                        QUERY, queryPredication.getParam(), regexp, Boolean.TRUE.toString());
                predicates.add(queryPredicate);
              });
    }

    if (request.getPredications().getRemoteAddresses() != null
        && !request.getPredications().getRemoteAddresses().isEmpty()) {
      String sources =
          request.getPredications().getRemoteAddresses().stream()
              .map(RemoteAddrPredication::getSource)
              .collect(Collectors.joining(","));
      predicates.add(definitionUtils.createPredicateDefinition(REMOTE_ADDR, sources));
    }

    if (request.getPredications().getWeights() != null
        && !request.getPredications().getWeights().isEmpty()) {
      request
          .getPredications()
          .getWeights()
          .forEach(
              weightPredication -> {
                predicates.add(
                    definitionUtils.createPredicateDefinition(
                        WEIGHT,
                        weightPredication.getGroup(),
                        weightPredication.getWeight().toString()));
              });
    }

    if (request.getPredications().getXforwardedRemoteAddresses() != null
        && !request.getPredications().getXforwardedRemoteAddresses().isEmpty()) {
      String sources =
          request.getPredications().getXforwardedRemoteAddresses().stream()
              .map(XForwardedRemoteAddrPredication::getSource)
              .collect(Collectors.joining(","));
      predicates.add(definitionUtils.createPredicateDefinition(X_FORWARDED_REMOTE_ADDR, sources));
    }

    if (request.getFilters().getAddRequestHeaders() != null
        && !request.getFilters().getAddRequestHeaders().isEmpty()) {
      request
          .getFilters()
          .getAddRequestHeaders()
          .forEach(
              addRequestHeaderFilter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        ADD_REQUEST_HEADER,
                        addRequestHeaderFilter.getName(),
                        addRequestHeaderFilter.getValue());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getAddRequestHeadersIfNotPresent() != null
        && !request.getFilters().getAddRequestHeadersIfNotPresent().isEmpty()) {
      String value =
          request.getFilters().getAddRequestHeadersIfNotPresent().stream()
              .map(f -> String.join(":", f.getName(), f.getValue()))
              .collect(Collectors.joining(","));
      filters.add(definitionUtils.createFilterDefinition(ADD_REQUEST_HEADER_IF_NOT_PRESENT, value));
    }

    if (request.getFilters().getAddRequestParameters() != null
        && !request.getFilters().getAddRequestParameters().isEmpty()) {
      request
          .getFilters()
          .getAddRequestParameters()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        ADD_REQUEST_PARAMETER, filter.getName(), filter.getValue());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getAddResponseHeaders() != null
        && !request.getFilters().getAddResponseHeaders().isEmpty()) {
      request
          .getFilters()
          .getAddResponseHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        ADD_RESPONSE_HEADER, filter.getName(), filter.getValue());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getCircuitBreakers() != null
        && !request.getFilters().getCircuitBreakers().isEmpty()) {
      request
          .getFilters()
          .getCircuitBreakers()
          .forEach(
              filter -> {
                String statusCodes = null;
                if (filter.getStatusCodes() != null && !filter.getStatusCodes().isEmpty()) {
                  statusCodes = String.join(",", filter.getStatusCodes());
                }
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        CIRCUIT_BREAKER, filter.getName(), filter.getFallbackUri(), statusCodes);
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getDedupeResponseHeaders() != null
        && !request.getFilters().getDedupeResponseHeaders().isEmpty()) {
      Map<DedupeStrategy, String> groupedByStrategy =
          request.getFilters().getDedupeResponseHeaders().stream()
              .collect(
                  Collectors.groupingBy(
                      DedupeResponseHeaderRequest::getStrategy,
                      Collectors.mapping(
                          DedupeResponseHeaderRequest::getName, Collectors.joining(" "))));
      groupedByStrategy.forEach(
          (strategy, name) -> {
            FilterDefinition filterDefinition =
                definitionUtils.createFilterDefinition(
                    DEDUPE_RESPONSE_HEADER, name, strategy.name());
            filters.add(filterDefinition);
          });
    }

    if (request.getFilters().getFallbackHeaders() != null
        && !request.getFilters().getFallbackHeaders().isEmpty()) {
      request
          .getFilters()
          .getFallbackHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        FALLBACK_HEADERS,
                        filter.getExecutionExceptionTypeHeaderName(),
                        filter.getExecutionExceptionMessageHeaderName(),
                        filter.getRootCauseExceptionTypeHeaderName(),
                        filter.getRootCauseExceptionMessageHeaderName());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getLocalResponseCache() != null
        && !request.getFilters().getLocalResponseCache().isEmpty()) {
      request
          .getFilters()
          .getLocalResponseCache()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        LOCAL_RESPONSE_CACHE, filter.getSize(), filter.getTimeToLive());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getMapRequestHeaders() != null
        && !request.getFilters().getMapRequestHeaders().isEmpty()) {
      request
          .getFilters()
          .getMapRequestHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        MAP_REQUEST_HEADER, filter.getFromHeader(), filter.getToHeader());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getPrefixPaths() != null
        && !request.getFilters().getPrefixPaths().isEmpty()) {
      request
          .getFilters()
          .getPrefixPaths()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(PREFIX_PATH, filter.getPrefix());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRedirects() != null
        && !request.getFilters().getRedirects().isEmpty()) {
      request
          .getFilters()
          .getRedirects()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REDIRECT_TO,
                        String.valueOf(filter.getStatus()),
                        filter.getUrl(),
                        String.valueOf(filter.isIncludeRequestParams()));
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRemoveJsonAttributesResponseBody() != null
        && !request.getFilters().getRemoveJsonAttributesResponseBody().isEmpty()) {
      request
          .getFilters()
          .getRemoveJsonAttributesResponseBody()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REMOVE_JSON_ATTRIBUTES_RESPONSE_BODY,
                        String.join(",", filter.getAttributes()),
                        String.valueOf(filter.isRecursive()));
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRemoveRequestHeaders() != null
        && !request.getFilters().getRemoveRequestHeaders().isEmpty()) {
      request
          .getFilters()
          .getRemoveRequestHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(REMOVE_REQUEST_HEADER, filter.getName());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRemoveRequestParameters() != null
        && !request.getFilters().getRemoveRequestParameters().isEmpty()) {
      request
          .getFilters()
          .getRemoveRequestParameters()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REMOVE_REQUEST_PARAMETER, filter.getName());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRemoveResponseHeaders() != null
        && !request.getFilters().getRemoveResponseHeaders().isEmpty()) {
      request
          .getFilters()
          .getRemoveResponseHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REMOVE_RESPONSE_HEADER, filter.getName());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRequestHeaderSizes() != null
        && !request.getFilters().getRequestHeaderSizes().isEmpty()) {
      request
          .getFilters()
          .getRequestHeaderSizes()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REQUEST_HEADER_SIZE, filter.getMaxSize(), filter.getErrorHeaderName());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRequestRateLimiter() != null) {
      FilterDefinition filterDefinition =
          definitionUtils.createFilterDefinition(
              REQUEST_RATE_LIMITER,
              String.valueOf(request.getFilters().getRequestRateLimiter().getReplenishRate()),
              String.valueOf(request.getFilters().getRequestRateLimiter().getBurstCapacity()),
              String.valueOf(request.getFilters().getRequestRateLimiter().getRequestedTokens()),
              request.getFilters().getRequestRateLimiter().getKeyResolver().getResolverName());

      if (KeyResolvers.CUSTOM_HEADER.equals(
          request.getFilters().getRequestRateLimiter().getKeyResolver())) {
        String headerName = request.getFilters().getRequestRateLimiter().getHeaderName();
        if (headerName == null || headerName.isEmpty()) {
          headerName = "X-Rate-Limit-Key";
        }
        routeDefinition.getMetadata().put("rate-limiter-header", headerName);
      }

      filters.add(filterDefinition);
    }

    if (request.getFilters().getRewriteLocationResponseHeaders() != null
        && !request.getFilters().getRewriteLocationResponseHeaders().isEmpty()) {
      request
          .getFilters()
          .getRewriteLocationResponseHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REWRITE_LOCATION_RESPONSE_HEADER,
                        filter.getStripVersionMode().name(),
                        filter.getLocationHeaderName(),
                        filter.getHostValue(),
                        filter.getProtocolsRegex());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRewritePaths() != null
        && !request.getFilters().getRewritePaths().isEmpty()) {
      request
          .getFilters()
          .getRewritePaths()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REWRITE_PATH, filter.getRegexp(), filter.getReplacement());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRewriteRequestParameters() != null
        && !request.getFilters().getRewriteRequestParameters().isEmpty()) {
      request
          .getFilters()
          .getRewriteRequestParameters()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REWRITE_REQUEST_PARAMETER, filter.getName(), filter.getReplacement());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getRewriteResponseHeaders() != null
        && !request.getFilters().getRewriteResponseHeaders().isEmpty()) {
      request
          .getFilters()
          .getRewriteResponseHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        REWRITE_RESPONSE_HEADER,
                        filter.getName(),
                        filter.getRegexp(),
                        filter.getReplacement());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getSetPath() != null) {
      FilterDefinition filterDefinition =
          definitionUtils.createFilterDefinition(
              SET_PATH, request.getFilters().getSetPath().getTemplate());
      filters.add(filterDefinition);
    }

    if (request.getFilters().getSetRequestHeaders() != null
        && !request.getFilters().getSetRequestHeaders().isEmpty()) {
      request
          .getFilters()
          .getSetRequestHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        SET_REQUEST_HEADER, filter.getName(), filter.getValue());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getSetResponseHeaders() != null
        && !request.getFilters().getSetResponseHeaders().isEmpty()) {
      request
          .getFilters()
          .getSetResponseHeaders()
          .forEach(
              filter -> {
                FilterDefinition filterDefinition =
                    definitionUtils.createFilterDefinition(
                        SET_RESPONSE_HEADER, filter.getName(), filter.getValue());
                filters.add(filterDefinition);
              });
    }

    if (request.getFilters().getSetStatus() != null) {
      FilterDefinition filterDefinition =
          definitionUtils.createFilterDefinition(
              SET_STATUS, String.valueOf(request.getFilters().getSetStatus().getStatus().value()));
      filters.add(filterDefinition);
    }

    if (request.getFilters().getStripPrefix() != null) {
      FilterDefinition filterDefinition =
          definitionUtils.createFilterDefinition(
              STRIP_PREFIX, request.getFilters().getStripPrefix().getParts().toString());
      filters.add(filterDefinition);
    }

    if (request.getFilters().getRetry() != null) {
      RetryRequest retry = request.getFilters().getRetry();
      String statuses = null;
      if (retry.getStatuses() != null && !retry.getStatuses().isEmpty()) {
        statuses =
            retry.getStatuses().stream().map(HttpStatus::name).collect(Collectors.joining(","));
      }
      String methods = null;
      if (retry.getMethods() != null && !retry.getMethods().isEmpty()) {
        methods = retry.getMethods().stream().map(String::valueOf).collect(Collectors.joining(","));
      }
      String series = null;
      if (retry.getSeries() != null && !retry.getSeries().isEmpty()) {
        series = retry.getSeries().stream().map(String::valueOf).collect(Collectors.joining(","));
      }
      FilterDefinition filterDefinition =
          definitionUtils.createFilterDefinition(
              RETRY,
              retry.getRetries(),
              statuses,
              methods,
              series,
              retry.getFirstBackoff(),
              retry.getMaxBackoff(),
              retry.getFactor(),
              retry.getBasedOnPreviousValue());
      filters.add(filterDefinition);
    }

    if (request.getSaveSessionEnabled() != null && request.getSaveSessionEnabled()) {
      FilterDefinition filterDefinition = definitionUtils.createFilterDefinition(SAVE_SESSION);
      filters.add(filterDefinition);
    }

    if (request.getFilters().getRequestSize() != null) {
      FilterDefinition filterDefinition =
          definitionUtils.createFilterDefinition(
              REQUEST_SIZE, String.valueOf(request.getFilters().getRequestSize().getMaxSize()));
      filters.add(filterDefinition);
    }

    if (request.getFilters().getSetRequestHostHeader() != null) {
      FilterDefinition filterDefinition =
          definitionUtils.createFilterDefinition(
              SET_REQUEST_HOST_HEADER, request.getFilters().getSetRequestHostHeader().getHost());
      filters.add(filterDefinition);
    }

    routeDefinition.setPredicates(predicates);
    routeDefinition.setFilters(filters);
    return routeDefinition;
  }
}
