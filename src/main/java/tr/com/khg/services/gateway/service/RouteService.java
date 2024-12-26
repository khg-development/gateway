package tr.com.khg.services.gateway.service;

import static tr.com.khg.services.gateway.entity.enums.FilterType.*;
import static tr.com.khg.services.gateway.entity.enums.PredicateType.*;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tr.com.khg.services.gateway.entity.*;
import tr.com.khg.services.gateway.entity.enums.DedupeStrategy;
import tr.com.khg.services.gateway.entity.enums.KeyResolvers;
import tr.com.khg.services.gateway.exception.DuplicateRouteException;
import tr.com.khg.services.gateway.model.request.*;
import tr.com.khg.services.gateway.model.response.*;
import tr.com.khg.services.gateway.repository.*;
import tr.com.khg.services.gateway.utils.DefinitionUtils;
import tr.com.khg.services.gateway.utils.FilterUtils;
import tr.com.khg.services.gateway.utils.PredicationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

  private static final String DEFAULT_REGEX = "^.+$";

  private final FilterUtils filterUtils;
  private final RouteRepository routeRepository;
  private final DefinitionUtils definitionUtils;
  private final PredicationUtils predicationUtils;
  private final ApiProxyRepository apiProxyRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final RouteDefinitionWriter routeDefinitionWriter;
  private final RouteCookiePredicationRepository cookieRepository;
  private final RouteHostPredicationRepository hostPredicationRepository;
  private final RouteQueryPredicationRepository queryPredicationRepository;
  private final RouteWeightPredicationRepository weightPredicationRepository;
  private final RouteHeaderPredicationRepository headerPredicationRepository;
  private final RouteRemoteAddrPredicationRepository remoteAddrPredicationRepository;
  private final RouteXForwardedRemoteAddrPredicationRepository forwardedAddrPredicationRepository;
  private final RouteAddRequestHeaderFilterRepository addRequestHeaderFilterRepository;
  private final RouteAddRequestHeaderIfNotPresentFilterRepository
      addRequestHeaderIfNotPresentFilterRepository;
  private final RouteAddRequestParameterFilterRepository addRequestParameterFilterRepository;
  private final RouteAddResponseHeaderFilterRepository addResponseHeaderFilterRepository;
  private final RouteCircuitBreakerFilterRepository circuitBreakerFilterRepository;
  private final RouteDedupeResponseHeaderFilterRepository dedupeResponseHeaderFilterRepository;
  private final RouteFallbackHeadersFilterRepository fallbackHeadersFilterRepository;
  private final RouteLocalResponseCacheFilterRepository localResponseCacheFilterRepository;
  private final RouteMapRequestHeaderFilterRepository mapRequestHeaderFilterRepository;
  private final RoutePrefixPathFilterRepository prefixPathFilterRepository;
  private final RouteRedirectToFilterRepository redirectToFilterRepository;
  private final RouteRemoveJsonAttributesResponseBodyFilterRepository
      removeJsonAttributesFilterRepository;
  private final RouteRemoveRequestHeaderFilterRepository removeRequestHeaderFilterRepository;
  private final RouteRemoveRequestParameterFilterRepository removeRequestParameterFilterRepository;
  private final RouteRemoveResponseHeaderFilterRepository removeResponseHeaderFilterRepository;
  private final RouteRequestHeaderSizeFilterRepository requestHeaderSizeFilterRepository;
  private final RouteRequestRateLimiterFilterRepository requestRateLimiterFilterRepository;
  private final RouteRewriteLocationResponseHeaderFilterRepository
      rewriteLocationResponseHeaderFilterRepository;
  private final RouteRewritePathFilterRepository rewritePathFilterRepository;
  private final RouteRewriteRequestParameterFilterRepository
      rewriteRequestParameterFilterRepository;
  private final RouteRewriteResponseHeaderFilterRepository rewriteResponseHeaderFilterRepository;
  private final RouteSetPathFilterRepository setPathFilterRepository;
  private final RouteSetRequestHeaderFilterRepository setRequestHeaderFilterRepository;
  private final RouteSetResponseHeaderFilterRepository setResponseHeaderFilterRepository;
  private final RouteSetStatusFilterRepository setStatusFilterRepository;
  private final RouteStripPrefixFilterRepository stripPrefixFilterRepository;

  @PostConstruct
  public void loadRoutesFromDatabase() {
    log.info("Loading routes from database");

    Mono.fromCallable(() -> routeRepository.findByEnabled(true))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapIterable(routes -> routes)
        .doOnNext(
            route ->
                log.debug("Loading route: {} - {}", route.getRouteId(), route.getRouteDefinition()))
        .flatMap(
            route ->
                routeDefinitionWriter
                    .save(Mono.just(route.getRouteDefinition()))
                    .doOnSuccess(
                        v -> log.debug("Route loaded successfully: {}", route.getRouteId()))
                    .doOnError(
                        error ->
                            log.error(
                                "Error loading route {}: {}",
                                route.getRouteId(),
                                error.getMessage())))
        .doOnComplete(() -> log.info("All routes loaded successfully"))
        .doOnError(error -> log.error("Error loading routes: {}", error.getMessage()))
        .subscribe();
  }

  @Transactional
  public Mono<RouteResponse> addRoute(String proxyName, RouteRequest request) {
    return Mono.fromCallable(
            () -> {
              validateUniqueRoute(proxyName, null, request);
              Route route = createRouteFromRequest(proxyName, request);
              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(this::applyNewRouteDefinition)
        .map(this::mapToRouteResponse)
        .doOnError(error -> log.error("Error adding route: {}", error.getMessage()));
  }

  @Transactional
  public Mono<RouteResponse> deleteRoute(String proxyName, String routeId) {
    return Mono.fromCallable(
            () -> {
              Route route = findRouteByProxyAndRouteId(proxyName, routeId);
              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(this::applyExistingRouteDefinition)
        .map(this::mapToRouteResponse);
  }

  @Transactional
  public Mono<RouteResponse> updateRouteStatus(String proxyName, String routeId, boolean enabled) {
    return Mono.fromCallable(
            () -> {
              Route route = findRouteByProxyAndRouteId(proxyName, routeId);
              route.setEnabled(enabled);
              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(this::applyExistingRouteDefinition)
        .map(this::mapToRouteResponse);
  }

  @Transactional(readOnly = true)
  public Mono<RoutesResponse> getRoutesByProxy(String proxyName) {
    return Mono.fromCallable(
            () ->
                routeRepository.findByApiProxyNameOrderById(proxyName).stream()
                    .map(this::mapToRouteResponse)
                    .collect(Collectors.toList()))
        .subscribeOn(Schedulers.boundedElastic())
        .map(routeResponses -> RoutesResponse.builder().routes(routeResponses).build())
        .doOnNext(
            response ->
                log.debug("Found {} routes for proxy: {}", response.getRoutes().size(), proxyName));
  }

  @Transactional
  public Mono<RouteResponse> updateRoute(String proxyName, RouteRequest request) {
    return Mono.fromCallable(
            () -> {
              Route existingRoute = findRouteByProxyAndRouteId(proxyName, request.getRouteId());
              validateUniqueRoute(proxyName, request.getRouteId(), request);
              updateRouteFromRequest(proxyName, existingRoute, request);
              return routeRepository.save(existingRoute);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(this::applyExistingRouteDefinition)
        .map(this::mapToRouteResponse)
        .doOnError(
            error ->
                log.error("Error updating route {}: {}", request.getRouteId(), error.getMessage()));
  }

  private Route findRouteByProxyAndRouteId(String proxyName, String routeId) {
    return routeRepository
        .findByApiProxyNameAndRouteId(proxyName, routeId)
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("Route not found: %s for proxy: %s", routeId, proxyName)));
  }

  private void validateUniqueRoute(String proxyName, String currentRouteId, RouteRequest request) {
    ApiProxy apiProxy =
        apiProxyRepository
            .findByName(proxyName)
            .orElseThrow(() -> new RuntimeException("Service not found: " + proxyName));

    routeRepository
        .findByApiProxyAndPathAndMethod(apiProxy, request.getPath(), request.getMethod())
        .ifPresent(
            r -> {
              if (!r.getRouteId().equals(currentRouteId)) {
                throw new DuplicateRouteException(
                    "Bu path ve HTTP metodu kombinasyonu zaten bu proxy için tanımlanmıştır: "
                        + request.getPath()
                        + " - "
                        + request.getMethod());
              }
            });
  }

  private Route createRouteFromRequest(String proxyName, RouteRequest request) {
    ApiProxy apiProxy =
        apiProxyRepository
            .findByName(proxyName)
            .orElseThrow(() -> new RuntimeException("Service not found: " + proxyName));

    Route route =
        Route.builder()
            .routeId(request.getRouteId())
            .apiProxy(apiProxy)
            .path(request.getPath())
            .method(request.getMethod())
            .enabled(true)
            .activationTime(request.getActivationTime())
            .expirationTime(request.getExpirationTime())
            .secureHeadersEnabled(request.getSecureHeadersEnabled())
            .matchTrailingSlash(
                request.getMatchTrailingSlash() != null ? request.getMatchTrailingSlash() : true)
            .routeCookiePredications(new ArrayList<>())
            .routeHeaderPredications(new ArrayList<>())
            .routeHostPredications(new ArrayList<>())
            .routeQueryPredications(new ArrayList<>())
            .routeRemoteAddrPredications(new ArrayList<>())
            .routeXForwardedRemoteAddrPredications(new ArrayList<>())
            .routeAddRequestHeaderFilters(new ArrayList<>())
            .routeAddRequestHeaderIfNotPresentFilters(new ArrayList<>())
            .routeAddRequestParameterFilters(new ArrayList<>())
            .routeAddResponseHeaderFilters(new ArrayList<>())
            .routeCircuitBreakerFilters(new ArrayList<>())
            .routeDedupeResponseHeaderFilters(new ArrayList<>())
            .routeFallbackHeadersFilters(new ArrayList<>())
            .routeLocalResponseCacheFilters(new ArrayList<>())
            .routeMapRequestHeaderFilters(new ArrayList<>())
            .routePrefixPathFilters(new ArrayList<>())
            .routeRedirectToFilters(new ArrayList<>())
            .routeRemoveJsonAttributesResponseBodyFilters(new ArrayList<>())
            .routeRemoveRequestHeaderFilters(new ArrayList<>())
            .routeRemoveRequestParameterFilters(new ArrayList<>())
            .routeRemoveResponseHeaderFilters(new ArrayList<>())
            .routeRequestHeaderSizeFilters(new ArrayList<>())
            .routeRequestRateLimiterFilter(null)
            .routeRewriteLocationResponseHeaderFilters(new ArrayList<>())
            .routeRewritePathFilters(new ArrayList<>())
            .routeRewriteRequestParameterFilters(new ArrayList<>())
            .routeRewriteResponseHeaderFilters(new ArrayList<>())
            .routeSetPathFilter(null)
            .routeSetRequestHeaderFilters(new ArrayList<>())
            .routeSetResponseHeaderFilters(new ArrayList<>())
            .routeSetStatusFilter(null)
            .routeStripPrefixFilter(null)
            .build();

    RouteDefinition routeDefinition = createRouteDefinition(request, apiProxy);
    route.setRouteDefinition(routeDefinition);

    Predications p = request.getPredications();
    route.setRouteCookiePredications(predicationUtils.createCookiePredications(p, route));
    route.setRouteHeaderPredications(predicationUtils.createHeaderPredications(p, route));
    route.setRouteHostPredications(predicationUtils.createHostPredications(p, route));
    route.setRouteQueryPredications(predicationUtils.createQueryPredications(p, route));
    route.setRouteRemoteAddrPredications(predicationUtils.createRemoteAddrPredications(p, route));
    route.setRouteWeightPredications(predicationUtils.createWeightPredications(p, route));
    route.setRouteXForwardedRemoteAddrPredications(
        predicationUtils.createXForwardedRemoteAddrPredications(p, route));

    Filters f = request.getFilters();
    route.setRouteAddRequestHeaderFilters(filterUtils.createAddRequestHeaderFilters(f, route));
    route.setRouteAddRequestHeaderIfNotPresentFilters(
        filterUtils.createAddRequestHeaderIfNotPresentFilters(f, route));
    route.setRouteAddRequestParameterFilters(
        filterUtils.createAddRequestParameterFilters(f, route));
    route.setRouteAddResponseHeaderFilters(filterUtils.createAddResponseHeaderFilters(f, route));
    route.setRouteCircuitBreakerFilters(filterUtils.createCircuitBreakerFilters(f, route));
    route.setRouteDedupeResponseHeaderFilters(
        filterUtils.createDedupeResponseHeaderFilters(f, route));
    route.setRouteFallbackHeadersFilters(filterUtils.createFallbackHeadersFilters(f, route));
    route.setRouteLocalResponseCacheFilters(filterUtils.createLocalResponseCacheFilters(f, route));
    route.setRouteMapRequestHeaderFilters(filterUtils.createMapRequestHeaderFilters(f, route));
    route.setRoutePrefixPathFilters(filterUtils.createPrefixPathFilters(f, route));
    route.setRouteRedirectToFilters(filterUtils.createRedirectToFilters(f, route));
    route.setRouteRemoveJsonAttributesResponseBodyFilters(
        filterUtils.createRemoveJsonAttributesResponseBodyFilters(f, route));
    route.setRouteRemoveRequestHeaderFilters(
        filterUtils.createRemoveRequestHeaderFilters(f, route));
    route.setRouteRemoveRequestParameterFilters(
        filterUtils.createRemoveRequestParameterFilters(f, route));
    route.setRouteRemoveResponseHeaderFilters(
        filterUtils.createRemoveResponseHeaderFilters(f, route));
    route.setRouteRequestHeaderSizeFilters(filterUtils.createRequestHeaderSizeFilters(f, route));
    route.setRouteRequestRateLimiterFilter(filterUtils.createRequestRateLimiterFilter(f, route));
    route.setRouteRewriteLocationResponseHeaderFilters(
        filterUtils.createRewriteLocationResponseHeaderFilters(f, route));
    route.setRouteRewritePathFilters(filterUtils.createRewritePathFilters(f, route));
    route.setRouteRewriteRequestParameterFilters(
        filterUtils.createRewriteRequestParameterFilters(f, route));
    route.setRouteRewriteResponseHeaderFilters(
        filterUtils.createRewriteResponseHeaderFilters(f, route));
    route.setRouteSetPathFilter(filterUtils.createSetPathFilter(f, route));
    route.setRouteSetRequestHeaderFilters(filterUtils.createSetRequestHeaderFilters(f, route));
    route.setRouteSetResponseHeaderFilters(filterUtils.createSetResponseHeaderFilters(f, route));
    route.setRouteSetStatusFilter(filterUtils.createSetStatusFilter(f, route));
    route.setRouteStripPrefixFilter(filterUtils.createStripPrefixFilter(f, route));

    return route;
  }

  private void updateRouteFromRequest(String proxyName, Route existingRoute, RouteRequest request) {
    ApiProxy apiProxy =
        apiProxyRepository
            .findByName(proxyName)
            .orElseThrow(() -> new RuntimeException("Service not found: " + proxyName));

    RouteDefinition routeDefinition = createRouteDefinition(request, apiProxy);
    existingRoute.setRouteDefinition(routeDefinition);
    existingRoute.setApiProxy(apiProxy);
    existingRoute.setPath(request.getPath());
    existingRoute.setMethod(request.getMethod());
    existingRoute.setActivationTime(request.getActivationTime());
    existingRoute.setExpirationTime(request.getExpirationTime());
    existingRoute.setSecureHeadersEnabled(request.getSecureHeadersEnabled());
    existingRoute.setMatchTrailingSlash(
        request.getMatchTrailingSlash() != null
            ? request.getMatchTrailingSlash()
            : existingRoute.isMatchTrailingSlash());

    existingRoute.clearPredications();
    existingRoute.clearFilters();
    cookieRepository.deleteByRoute(existingRoute);
    headerPredicationRepository.deleteByRoute(existingRoute);
    hostPredicationRepository.deleteByRoute(existingRoute);
    queryPredicationRepository.deleteByRoute(existingRoute);
    remoteAddrPredicationRepository.deleteByRoute(existingRoute);
    weightPredicationRepository.deleteByRoute(existingRoute);
    forwardedAddrPredicationRepository.deleteByRoute(existingRoute);
    addRequestHeaderFilterRepository.deleteByRoute(existingRoute);
    addRequestHeaderIfNotPresentFilterRepository.deleteByRoute(existingRoute);
    addRequestParameterFilterRepository.deleteByRoute(existingRoute);
    addResponseHeaderFilterRepository.deleteByRoute(existingRoute);
    circuitBreakerFilterRepository.deleteByRoute(existingRoute);
    dedupeResponseHeaderFilterRepository.deleteByRoute(existingRoute);
    fallbackHeadersFilterRepository.deleteByRoute(existingRoute);
    localResponseCacheFilterRepository.deleteByRoute(existingRoute);
    mapRequestHeaderFilterRepository.deleteByRoute(existingRoute);
    prefixPathFilterRepository.deleteByRoute(existingRoute);
    redirectToFilterRepository.deleteByRoute(existingRoute);
    removeJsonAttributesFilterRepository.deleteByRoute(existingRoute);
    removeRequestHeaderFilterRepository.deleteByRoute(existingRoute);
    removeRequestParameterFilterRepository.deleteByRoute(existingRoute);
    removeResponseHeaderFilterRepository.deleteByRoute(existingRoute);
    requestHeaderSizeFilterRepository.deleteByRoute(existingRoute);
    requestRateLimiterFilterRepository.deleteByRoute(existingRoute);
    rewriteLocationResponseHeaderFilterRepository.deleteByRoute(existingRoute);
    rewritePathFilterRepository.deleteByRoute(existingRoute);
    rewriteRequestParameterFilterRepository.deleteByRoute(existingRoute);
    rewriteResponseHeaderFilterRepository.deleteByRoute(existingRoute);
    setPathFilterRepository.deleteByRoute(existingRoute);
    setRequestHeaderFilterRepository.deleteByRoute(existingRoute);
    setResponseHeaderFilterRepository.deleteByRoute(existingRoute);
    setStatusFilterRepository.deleteByRoute(existingRoute);
    stripPrefixFilterRepository.deleteByRoute(existingRoute);

    Predications p = request.getPredications();
    existingRoute.setRouteCookiePredications(
        predicationUtils.createCookiePredications(p, existingRoute));
    existingRoute.setRouteHeaderPredications(
        predicationUtils.createHeaderPredications(p, existingRoute));
    existingRoute.setRouteHostPredications(
        predicationUtils.createHostPredications(p, existingRoute));
    existingRoute.setRouteQueryPredications(
        predicationUtils.createQueryPredications(p, existingRoute));
    existingRoute.setRouteRemoteAddrPredications(
        predicationUtils.createRemoteAddrPredications(p, existingRoute));
    existingRoute.setRouteWeightPredications(
        predicationUtils.createWeightPredications(p, existingRoute));
    existingRoute.setRouteXForwardedRemoteAddrPredications(
        predicationUtils.createXForwardedRemoteAddrPredications(p, existingRoute));

    Filters f = request.getFilters();
    existingRoute.setRouteAddRequestHeaderFilters(
        filterUtils.createAddRequestHeaderFilters(f, existingRoute));
    existingRoute.setRouteAddRequestHeaderIfNotPresentFilters(
        filterUtils.createAddRequestHeaderIfNotPresentFilters(f, existingRoute));
    existingRoute.setRouteAddRequestParameterFilters(
        filterUtils.createAddRequestParameterFilters(f, existingRoute));
    existingRoute.setRouteAddResponseHeaderFilters(
        filterUtils.createAddResponseHeaderFilters(f, existingRoute));
    existingRoute.setRouteCircuitBreakerFilters(
        filterUtils.createCircuitBreakerFilters(f, existingRoute));
    existingRoute.setRouteDedupeResponseHeaderFilters(
        filterUtils.createDedupeResponseHeaderFilters(f, existingRoute));
    existingRoute.setRouteFallbackHeadersFilters(
        filterUtils.createFallbackHeadersFilters(f, existingRoute));
    existingRoute.setRouteLocalResponseCacheFilters(
        filterUtils.createLocalResponseCacheFilters(f, existingRoute));
    existingRoute.setRouteMapRequestHeaderFilters(
        filterUtils.createMapRequestHeaderFilters(f, existingRoute));
    existingRoute.setRoutePrefixPathFilters(filterUtils.createPrefixPathFilters(f, existingRoute));
    existingRoute.setRouteRedirectToFilters(filterUtils.createRedirectToFilters(f, existingRoute));
    existingRoute.setRouteRemoveJsonAttributesResponseBodyFilters(
        filterUtils.createRemoveJsonAttributesResponseBodyFilters(f, existingRoute));
    existingRoute.setRouteRemoveRequestHeaderFilters(
        filterUtils.createRemoveRequestHeaderFilters(f, existingRoute));
    existingRoute.setRouteRemoveRequestParameterFilters(
        filterUtils.createRemoveRequestParameterFilters(f, existingRoute));
    existingRoute.setRouteRemoveResponseHeaderFilters(
        filterUtils.createRemoveResponseHeaderFilters(f, existingRoute));
    existingRoute.setRouteRequestHeaderSizeFilters(
        filterUtils.createRequestHeaderSizeFilters(f, existingRoute));
    existingRoute.setRouteRequestRateLimiterFilter(
        filterUtils.createRequestRateLimiterFilter(f, existingRoute));
    existingRoute.setRouteRewriteLocationResponseHeaderFilters(
        filterUtils.createRewriteLocationResponseHeaderFilters(f, existingRoute));
    existingRoute.setRouteRewritePathFilters(
        filterUtils.createRewritePathFilters(f, existingRoute));
    existingRoute.setRouteRewriteRequestParameterFilters(
        filterUtils.createRewriteRequestParameterFilters(f, existingRoute));
    existingRoute.setRouteRewriteResponseHeaderFilters(
        filterUtils.createRewriteResponseHeaderFilters(f, existingRoute));
    existingRoute.setRouteSetPathFilter(filterUtils.createSetPathFilter(f, existingRoute));
    existingRoute.setRouteSetRequestHeaderFilters(
        filterUtils.createSetRequestHeaderFilters(f, existingRoute));
    existingRoute.setRouteSetResponseHeaderFilters(
        filterUtils.createSetResponseHeaderFilters(f, existingRoute));
    existingRoute.setRouteSetStatusFilter(filterUtils.createSetStatusFilter(f, existingRoute));
    existingRoute.setRouteStripPrefixFilter(filterUtils.createStripPrefixFilter(f, existingRoute));
  }

  private RouteDefinition createRouteDefinition(RouteRequest request, ApiProxy apiProxy) {
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
      String hostPatterns =
          request.getPredications().getHosts().stream()
              .map(HostPredication::getPattern)
              .collect(Collectors.joining(","));
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
                        : DEFAULT_REGEX;
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

    routeDefinition.setPredicates(predicates);
    routeDefinition.setFilters(filters);
    return routeDefinition;
  }

  private Mono<Route> applyNewRouteDefinition(Route route) {
    return routeDefinitionWriter
        .save(Mono.just(route.getRouteDefinition()))
        .then(Mono.fromRunnable(() -> eventPublisher.publishEvent(new RefreshRoutesEvent(this))))
        .thenReturn(route)
        .doOnError(
            error -> log.error("Error applying new route definition: {}", error.getMessage()));
  }

  private Mono<Route> applyExistingRouteDefinition(Route route) {
    return routeDefinitionWriter
        .delete(Mono.just(route.getRouteId()))
        .onErrorResume(
            ex -> {
              log.warn("Route definition not found for deletion: {}", route.getRouteId());
              return Mono.empty();
            })
        .then(routeDefinitionWriter.save(Mono.just(route.getRouteDefinition())))
        .then(Mono.fromRunnable(() -> eventPublisher.publishEvent(new RefreshRoutesEvent(this))))
        .thenReturn(route)
        .doOnError(
            error -> log.error("Error applying existing route definition: {}", error.getMessage()));
  }

  private RouteResponse mapToRouteResponse(Route route) {
    List<CookiePredicationResponse> cookiePredicationResponses = new ArrayList<>();
    if (route.getRouteCookiePredications() != null) {
      cookiePredicationResponses =
          route.getRouteCookiePredications().stream()
              .map(
                  cookiePredication ->
                      CookiePredicationResponse.builder()
                          .name(cookiePredication.getName())
                          .regexp(cookiePredication.getRegexp())
                          .build())
              .toList();
    }

    List<HeaderPredicationResponse> headerPredicationResponses = new ArrayList<>();
    if (route.getRouteHeaderPredications() != null) {
      headerPredicationResponses =
          route.getRouteHeaderPredications().stream()
              .map(
                  headerPredication ->
                      HeaderPredicationResponse.builder()
                          .name(headerPredication.getName())
                          .regexp(headerPredication.getRegexp())
                          .build())
              .toList();
    }

    List<HostPredicationResponse> hostPredicationResponses = new ArrayList<>();
    if (route.getRouteHostPredications() != null) {
      hostPredicationResponses =
          route.getRouteHostPredications().stream()
              .map(
                  hostPredication ->
                      HostPredicationResponse.builder()
                          .pattern(hostPredication.getPattern())
                          .build())
              .toList();
    }

    List<QueryPredicationResponse> queryPredicationResponses = new ArrayList<>();
    if (route.getRouteQueryPredications() != null) {
      queryPredicationResponses =
          route.getRouteQueryPredications().stream()
              .map(
                  queryPredication ->
                      QueryPredicationResponse.builder()
                          .param(queryPredication.getParam())
                          .regexp(queryPredication.getRegexp())
                          .build())
              .toList();
    }

    List<RemoteAddrPredicationResponse> remoteAddrPredicationResponses = new ArrayList<>();
    if (route.getRouteRemoteAddrPredications() != null) {
      remoteAddrPredicationResponses =
          route.getRouteRemoteAddrPredications().stream()
              .map(
                  remoteAddrPredication ->
                      RemoteAddrPredicationResponse.builder()
                          .source(remoteAddrPredication.getSource())
                          .build())
              .toList();
    }

    List<WeightPredicationResponse> weightPredicationResponses = new ArrayList<>();
    if (route.getRouteWeightPredications() != null) {
      weightPredicationResponses =
          route.getRouteWeightPredications().stream()
              .map(
                  weightPredication ->
                      WeightPredicationResponse.builder()
                          .group(weightPredication.getGroup())
                          .weight(weightPredication.getWeight())
                          .build())
              .toList();
    }

    List<XForwardedRemoteAddrPredicationResponse> xForwardedRemoteAddrPredicationResponses =
        new ArrayList<>();
    if (route.getRouteXForwardedRemoteAddrPredications() != null) {
      xForwardedRemoteAddrPredicationResponses =
          route.getRouteXForwardedRemoteAddrPredications().stream()
              .map(
                  predication ->
                      XForwardedRemoteAddrPredicationResponse.builder()
                          .source(predication.getSource())
                          .build())
              .toList();
    }

    List<AddRequestHeaderFilterResponse> addRequestHeaderResponses = new ArrayList<>();
    if (route.getRouteAddRequestHeaderFilters() != null) {
      addRequestHeaderResponses =
          route.getRouteAddRequestHeaderFilters().stream()
              .map(
                  filter ->
                      AddRequestHeaderFilterResponse.builder()
                          .name(filter.getName())
                          .value(filter.getValue())
                          .build())
              .toList();
    }

    List<AddRequestHeaderIfNotPresentFilterResponse> addRequestHeaderIfNotPresentResponses =
        new ArrayList<>();
    if (route.getRouteAddRequestHeaderIfNotPresentFilters() != null) {
      addRequestHeaderIfNotPresentResponses =
          route.getRouteAddRequestHeaderIfNotPresentFilters().stream()
              .map(
                  filter ->
                      AddRequestHeaderIfNotPresentFilterResponse.builder()
                          .name(filter.getName())
                          .value(filter.getValue())
                          .build())
              .toList();
    }

    List<AddRequestParameterFilterResponse> addRequestParameterResponses = new ArrayList<>();
    if (route.getRouteAddRequestParameterFilters() != null) {
      addRequestParameterResponses =
          route.getRouteAddRequestParameterFilters().stream()
              .map(
                  filter ->
                      AddRequestParameterFilterResponse.builder()
                          .name(filter.getName())
                          .value(filter.getValue())
                          .build())
              .toList();
    }

    List<AddResponseHeaderFilterResponse> addResponseHeaderResponses = new ArrayList<>();
    if (route.getRouteAddResponseHeaderFilters() != null) {
      addResponseHeaderResponses =
          route.getRouteAddResponseHeaderFilters().stream()
              .map(
                  filter ->
                      AddResponseHeaderFilterResponse.builder()
                          .name(filter.getName())
                          .value(filter.getValue())
                          .build())
              .toList();
    }

    List<CircuitBreakerResponse> circuitBreakerResponses = new ArrayList<>();
    if (route.getRouteCircuitBreakerFilters() != null) {
      circuitBreakerResponses =
          route.getRouteCircuitBreakerFilters().stream()
              .map(
                  filter ->
                      CircuitBreakerResponse.builder()
                          .name(filter.getName())
                          .fallbackUri(filter.getFallbackUri())
                          .statusCodes(
                              filter.getStatusCodes() != null
                                  ? Arrays.asList(filter.getStatusCodes().split(","))
                                  : null)
                          .build())
              .toList();
    }

    List<DedupeResponseHeaderResponse> dedupeResponseHeaderResponses = new ArrayList<>();
    if (route.getRouteDedupeResponseHeaderFilters() != null) {
      dedupeResponseHeaderResponses =
          route.getRouteDedupeResponseHeaderFilters().stream()
              .map(
                  filter ->
                      DedupeResponseHeaderResponse.builder()
                          .name(filter.getName())
                          .strategy(filter.getStrategy())
                          .build())
              .toList();
    }

    List<FallbackHeadersResponse> fallbackHeadersResponses = new ArrayList<>();
    if (route.getRouteFallbackHeadersFilters() != null) {
      fallbackHeadersResponses =
          route.getRouteFallbackHeadersFilters().stream()
              .map(
                  filter ->
                      FallbackHeadersResponse.builder()
                          .executionExceptionTypeHeaderName(
                              filter.getExecutionExceptionTypeHeaderName())
                          .executionExceptionMessageHeaderName(
                              filter.getExecutionExceptionMessageHeaderName())
                          .rootCauseExceptionTypeHeaderName(
                              filter.getRootCauseExceptionTypeHeaderName())
                          .rootCauseExceptionMessageHeaderName(
                              filter.getRootCauseExceptionMessageHeaderName())
                          .build())
              .toList();
    }

    List<LocalResponseCacheResponse> localResponseCacheResponses = new ArrayList<>();
    if (route.getRouteLocalResponseCacheFilters() != null) {
      localResponseCacheResponses =
          route.getRouteLocalResponseCacheFilters().stream()
              .map(
                  filter ->
                      LocalResponseCacheResponse.builder()
                          .size(filter.getSize())
                          .timeToLive(filter.getTimeToLive())
                          .build())
              .toList();
    }

    List<MapRequestHeaderResponse> mapRequestHeaderResponses = new ArrayList<>();
    if (route.getRouteMapRequestHeaderFilters() != null) {
      mapRequestHeaderResponses =
          route.getRouteMapRequestHeaderFilters().stream()
              .map(
                  filter ->
                      MapRequestHeaderResponse.builder()
                          .fromHeader(filter.getFromHeader())
                          .toHeader(filter.getToHeader())
                          .build())
              .toList();
    }

    List<PrefixPathResponse> prefixPathResponses = new ArrayList<>();
    if (route.getRoutePrefixPathFilters() != null) {
      prefixPathResponses =
          route.getRoutePrefixPathFilters().stream()
              .map(filter -> PrefixPathResponse.builder().prefix(filter.getPrefix()).build())
              .toList();
    }

    List<RedirectToResponse> redirectToResponses = new ArrayList<>();
    if (route.getRouteRedirectToFilters() != null) {
      redirectToResponses =
          route.getRouteRedirectToFilters().stream()
              .map(
                  filter ->
                      RedirectToResponse.builder()
                          .status(filter.getStatus())
                          .url(filter.getUrl())
                          .includeRequestParams(filter.isIncludeRequestParams())
                          .build())
              .toList();
    }

    List<RemoveJsonAttributesResponseBodyResponse> removeJsonAttributesResponses =
        new ArrayList<>();
    if (route.getRouteRemoveJsonAttributesResponseBodyFilters() != null) {
      removeJsonAttributesResponses =
          route.getRouteRemoveJsonAttributesResponseBodyFilters().stream()
              .map(
                  filter ->
                      RemoveJsonAttributesResponseBodyResponse.builder()
                          .attributes(
                              filter.getAttributes() != null
                                  ? Arrays.asList(filter.getAttributes().split(","))
                                  : null)
                          .recursive(filter.isRecursive())
                          .build())
              .toList();
    }

    List<RemoveRequestHeaderResponse> removeRequestHeaderResponses = new ArrayList<>();
    if (route.getRouteRemoveRequestHeaderFilters() != null) {
      removeRequestHeaderResponses =
          route.getRouteRemoveRequestHeaderFilters().stream()
              .map(filter -> RemoveRequestHeaderResponse.builder().name(filter.getName()).build())
              .toList();
    }

    List<RemoveRequestParameterResponse> removeRequestParameterResponses = new ArrayList<>();
    if (route.getRouteRemoveRequestParameterFilters() != null) {
      removeRequestParameterResponses =
          route.getRouteRemoveRequestParameterFilters().stream()
              .map(
                  filter -> RemoveRequestParameterResponse.builder().name(filter.getName()).build())
              .toList();
    }

    List<RemoveResponseHeaderResponse> removeResponseHeaderResponses = new ArrayList<>();
    if (route.getRouteRemoveResponseHeaderFilters() != null) {
      removeResponseHeaderResponses =
          route.getRouteRemoveResponseHeaderFilters().stream()
              .map(filter -> RemoveResponseHeaderResponse.builder().name(filter.getName()).build())
              .toList();
    }

    List<RequestHeaderSizeResponse> requestHeaderSizeResponses = new ArrayList<>();
    if (route.getRouteRequestHeaderSizeFilters() != null) {
      requestHeaderSizeResponses =
          route.getRouteRequestHeaderSizeFilters().stream()
              .map(
                  filter ->
                      RequestHeaderSizeResponse.builder()
                          .maxSize(filter.getMaxSize())
                          .errorHeaderName(filter.getErrorHeaderName())
                          .build())
              .toList();
    }

    RequestRateLimiterResponse requestRateLimiterResponse = null;
    if (route.getRouteRequestRateLimiterFilter() != null) {
      requestRateLimiterResponse =
          RequestRateLimiterResponse.builder()
              .replenishRate(route.getRouteRequestRateLimiterFilter().getReplenishRate())
              .burstCapacity(route.getRouteRequestRateLimiterFilter().getBurstCapacity())
              .requestedTokens(route.getRouteRequestRateLimiterFilter().getRequestedTokens())
              .keyResolver(route.getRouteRequestRateLimiterFilter().getKeyResolver())
              .headerName(route.getRouteRequestRateLimiterFilter().getHeaderName())
              .build();
    }

    List<RewriteLocationResponseHeaderResponse> rewriteLocationResponseHeaderResponses =
        new ArrayList<>();
    if (route.getRouteRewriteLocationResponseHeaderFilters() != null) {
      rewriteLocationResponseHeaderResponses =
          route.getRouteRewriteLocationResponseHeaderFilters().stream()
              .map(
                  filter ->
                      RewriteLocationResponseHeaderResponse.builder()
                          .stripVersionMode(filter.getStripVersionMode())
                          .locationHeaderName(filter.getLocationHeaderName())
                          .hostValue(filter.getHostValue())
                          .protocolsRegex(filter.getProtocolsRegex())
                          .build())
              .toList();
    }

    List<RewritePathResponse> rewritePathResponses = new ArrayList<>();
    if (route.getRouteRewritePathFilters() != null) {
      rewritePathResponses =
          route.getRouteRewritePathFilters().stream()
              .map(
                  filter ->
                      RewritePathResponse.builder()
                          .regexp(filter.getRegexp())
                          .replacement(filter.getReplacement())
                          .build())
              .toList();
    }

    List<RewriteRequestParameterResponse> rewriteRequestParameterResponses = new ArrayList<>();
    if (route.getRouteRewriteRequestParameterFilters() != null) {
      rewriteRequestParameterResponses =
          route.getRouteRewriteRequestParameterFilters().stream()
              .map(
                  filter ->
                      RewriteRequestParameterResponse.builder()
                          .name(filter.getName())
                          .replacement(filter.getReplacement())
                          .build())
              .toList();
    }

    List<RewriteResponseHeaderResponse> rewriteResponseHeaderResponses = new ArrayList<>();
    if (route.getRouteRewriteResponseHeaderFilters() != null) {
      rewriteResponseHeaderResponses =
          route.getRouteRewriteResponseHeaderFilters().stream()
              .map(
                  filter ->
                      RewriteResponseHeaderResponse.builder()
                          .name(filter.getName())
                          .regexp(filter.getRegexp())
                          .replacement(filter.getReplacement())
                          .build())
              .toList();
    }

    SetPathResponse setPathResponse = null;
    if (route.getRouteSetPathFilter() != null) {
      setPathResponse =
          SetPathResponse.builder().template(route.getRouteSetPathFilter().getTemplate()).build();
    }

    List<SetRequestHeaderResponse> setRequestHeaderResponses = new ArrayList<>();
    if (route.getRouteSetRequestHeaderFilters() != null) {
      setRequestHeaderResponses =
          route.getRouteSetRequestHeaderFilters().stream()
              .map(
                  filter ->
                      SetRequestHeaderResponse.builder()
                          .name(filter.getName())
                          .value(filter.getValue())
                          .build())
              .toList();
    }

    List<SetResponseHeaderResponse> setResponseHeaderResponses = new ArrayList<>();
    if (route.getRouteSetResponseHeaderFilters() != null) {
      setResponseHeaderResponses =
          route.getRouteSetResponseHeaderFilters().stream()
              .map(
                  filter ->
                      SetResponseHeaderResponse.builder()
                          .name(filter.getName())
                          .value(filter.getValue())
                          .build())
              .toList();
    }

    SetStatusResponse setStatusResponse = null;
    if (route.getRouteSetStatusFilter() != null) {
      setStatusResponse =
          SetStatusResponse.builder().status(route.getRouteSetStatusFilter().getStatus()).build();
    }

    StripPrefixResponse stripPrefixResponse = null;
    if (route.getRouteStripPrefixFilter() != null) {
      stripPrefixResponse =
          StripPrefixResponse.builder().parts(route.getRouteStripPrefixFilter().getParts()).build();
    }

    return RouteResponse.builder()
        .routeId(route.getRouteId())
        .enabled(route.isEnabled())
        .path(route.getPath())
        .method(route.getMethod())
        .matchTrailingSlash(route.isMatchTrailingSlash())
        .activationTime(route.getActivationTime())
        .expirationTime(route.getExpirationTime())
        .bodyLogEnabled(route.isBodyLogEnabled())
        .preserveHostHeader(route.isPreserveHostHeader())
        .secureHeadersEnabled(route.isSecureHeadersEnabled())
        .predications(
            PredicationsResponse.builder()
                .cookies(cookiePredicationResponses)
                .headers(headerPredicationResponses)
                .hosts(hostPredicationResponses)
                .queries(queryPredicationResponses)
                .remoteAddresses(remoteAddrPredicationResponses)
                .weights(weightPredicationResponses)
                .xforwardedRemoteAddresses(xForwardedRemoteAddrPredicationResponses)
                .build())
        .filters(
            FiltersResponse.builder()
                .addRequestHeaders(addRequestHeaderResponses)
                .addRequestHeadersIfNotPresent(addRequestHeaderIfNotPresentResponses)
                .addRequestParameters(addRequestParameterResponses)
                .addResponseHeaders(addResponseHeaderResponses)
                .circuitBreakers(circuitBreakerResponses)
                .dedupeResponseHeaders(dedupeResponseHeaderResponses)
                .fallbackHeaders(fallbackHeadersResponses)
                .localResponseCache(localResponseCacheResponses)
                .mapRequestHeaders(mapRequestHeaderResponses)
                .prefixPaths(prefixPathResponses)
                .redirects(redirectToResponses)
                .removeJsonAttributesResponseBody(removeJsonAttributesResponses)
                .removeRequestHeaders(removeRequestHeaderResponses)
                .removeRequestParameters(removeRequestParameterResponses)
                .removeResponseHeaders(removeResponseHeaderResponses)
                .requestHeaderSizes(requestHeaderSizeResponses)
                .requestRateLimiter(requestRateLimiterResponse)
                .rewriteLocationResponseHeaders(rewriteLocationResponseHeaderResponses)
                .rewritePaths(rewritePathResponses)
                .rewriteRequestParameters(rewriteRequestParameterResponses)
                .rewriteResponseHeaders(rewriteResponseHeaderResponses)
                .setPath(setPathResponse)
                .setRequestHeaders(setRequestHeaderResponses)
                .setResponseHeaders(setResponseHeaderResponses)
                .setStatus(setStatusResponse)
                .stripPrefix(stripPrefixResponse)
                .build())
        .build();
  }
}
