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
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteCookiePredication;
import tr.com.khg.services.gateway.entity.RouteHeaderConfiguration;
import tr.com.khg.services.gateway.entity.RouteHeaderPredication;
import tr.com.khg.services.gateway.entity.RouteHostPredication;
import tr.com.khg.services.gateway.entity.RouteQueryPredication;
import tr.com.khg.services.gateway.entity.RouteRemoteAddrPredication;
import tr.com.khg.services.gateway.entity.RouteWeightPredication;
import tr.com.khg.services.gateway.entity.RouteXForwardedRemoteAddrPredication;
import tr.com.khg.services.gateway.entity.enums.FilterType;
import tr.com.khg.services.gateway.exception.DuplicateRouteException;
import tr.com.khg.services.gateway.model.HeaderConfiguration;
import tr.com.khg.services.gateway.model.request.HostPredication;
import tr.com.khg.services.gateway.model.request.RemoteAddrPredication;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.model.request.XForwardedRemoteAddrPredication;
import tr.com.khg.services.gateway.model.response.CookiePredicationResponse;
import tr.com.khg.services.gateway.model.response.HeaderPredicationResponse;
import tr.com.khg.services.gateway.model.response.HostPredicationResponse;
import tr.com.khg.services.gateway.model.response.QueryPredicationResponse;
import tr.com.khg.services.gateway.model.response.RemoteAddrPredicationResponse;
import tr.com.khg.services.gateway.model.response.RouteResponse;
import tr.com.khg.services.gateway.model.response.RoutesResponse;
import tr.com.khg.services.gateway.model.response.WeightPredicationResponse;
import tr.com.khg.services.gateway.model.response.XForwardedRemoteAddrPredicationResponse;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteCookiePredicationRepository;
import tr.com.khg.services.gateway.repository.RouteHeaderConfigurationRepository;
import tr.com.khg.services.gateway.repository.RouteHeaderPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteHostPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteQueryPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteRemoteAddrPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.repository.RouteWeightPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteXForwardedRemoteAddrPredicationRepository;
import tr.com.khg.services.gateway.utils.DefinitionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

  private static final String DEFAULT_REGEX = "^.+$";

  private final RouteRepository routeRepository;
  private final DefinitionUtils definitionUtils;
  private final ApiProxyRepository apiProxyRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final RouteDefinitionWriter routeDefinitionWriter;
  private final RouteHeaderConfigurationRepository headerRepository;
  private final RouteCookiePredicationRepository cookieRepository;
  private final RouteHeaderPredicationRepository headerPredicationRepository;
  private final RouteHostPredicationRepository hostPredicationRepository;
  private final RouteQueryPredicationRepository queryPredicationRepository;
  private final RouteRemoteAddrPredicationRepository remoteAddrPredicationRepository;
  private final RouteWeightPredicationRepository weightPredicationRepository;
  private final RouteXForwardedRemoteAddrPredicationRepository forwardedAddrPredicationRepository;

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

  @Transactional(readOnly = true)
  public Mono<RouteResponse> getRoute(String proxyName, String routeId) {
    return Mono.fromCallable(() -> findRouteByProxyAndRouteId(proxyName, routeId))
        .map(this::mapToRouteResponse)
        .subscribeOn(Schedulers.boundedElastic());
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
            .matchTrailingSlash(
                request.getMatchTrailingSlash() != null ? request.getMatchTrailingSlash() : true)
            .routeCookiePredications(new ArrayList<>())
            .routeHeaderConfigurations(new ArrayList<>())
            .routeHeaderPredications(new ArrayList<>())
            .routeHostPredications(new ArrayList<>())
            .routeQueryPredications(new ArrayList<>())
            .routeRemoteAddrPredications(new ArrayList<>())
            .routeXForwardedRemoteAddrPredications(new ArrayList<>())
            .build();

    RouteDefinition routeDefinition = createRouteDefinition(request, apiProxy);
    route.setRouteDefinition(routeDefinition);

    if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
      List<RouteHeaderConfiguration> headerConfigurations =
          createHeaderConfigurations(request, route);
      route.setRouteHeaderConfigurations(headerConfigurations);
    }

    if (request.getCookiePredications() != null && !request.getCookiePredications().isEmpty()) {
      List<RouteCookiePredication> cookiePredications = createCookiePredications(request, route);
      route.setRouteCookiePredications(cookiePredications);
    }

    if (request.getHeaderPredications() != null && !request.getHeaderPredications().isEmpty()) {
      List<RouteHeaderPredication> headerPredications = createHeaderPredications(request, route);
      route.setRouteHeaderPredications(headerPredications);
    }

    if (request.getHostPredications() != null && !request.getHostPredications().isEmpty()) {
      List<RouteHostPredication> hostPredications = createHostPredications(request, route);
      route.setRouteHostPredications(hostPredications);
    }

    if (request.getQueryPredications() != null && !request.getQueryPredications().isEmpty()) {
      List<RouteQueryPredication> queryPredications = createQueryPredications(request, route);
      route.setRouteQueryPredications(queryPredications);
    }

    if (request.getRemoteAddrPredications() != null
        && !request.getRemoteAddrPredications().isEmpty()) {
      List<RouteRemoteAddrPredication> remoteAddrPredications =
          createRemoteAddrPredications(request, route);
      route.setRouteRemoteAddrPredications(remoteAddrPredications);
    }

    if (request.getWeightPredications() != null && !request.getWeightPredications().isEmpty()) {
      List<RouteWeightPredication> weightPredications = createWeightPredications(request, route);
      route.setRouteWeightPredications(weightPredications);
    }

    if (request.getXForwardedRemoteAddrPredications() != null
        && !request.getXForwardedRemoteAddrPredications().isEmpty()) {
      List<RouteXForwardedRemoteAddrPredication> xForwardedRemoteAddrPredications =
          createXForwardedRemoteAddrPredications(request, route);
      route.setRouteXForwardedRemoteAddrPredications(xForwardedRemoteAddrPredications);
    }

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
    existingRoute.setMatchTrailingSlash(
        request.getMatchTrailingSlash() != null
            ? request.getMatchTrailingSlash()
            : existingRoute.isMatchTrailingSlash());

    existingRoute.getRouteHeaderConfigurations().clear();
    existingRoute.getRouteCookiePredications().clear();
    existingRoute.getRouteHeaderPredications().clear();
    existingRoute.getRouteHostPredications().clear();
    existingRoute.getRouteQueryPredications().clear();
    existingRoute.getRouteRemoteAddrPredications().clear();
    existingRoute.getRouteXForwardedRemoteAddrPredications().clear();

    headerRepository.deleteByRoute(existingRoute);
    cookieRepository.deleteByRoute(existingRoute);
    headerPredicationRepository.deleteByRoute(existingRoute);
    hostPredicationRepository.deleteByRoute(existingRoute);
    queryPredicationRepository.deleteByRoute(existingRoute);
    remoteAddrPredicationRepository.deleteByRoute(existingRoute);
    weightPredicationRepository.deleteByRoute(existingRoute);
    forwardedAddrPredicationRepository.deleteByRoute(existingRoute);

    existingRoute.setRouteHeaderConfigurations(createHeaderConfigurations(request, existingRoute));
    existingRoute.setRouteCookiePredications(createCookiePredications(request, existingRoute));
    existingRoute.setRouteHeaderPredications(createHeaderPredications(request, existingRoute));
    existingRoute.setRouteHostPredications(createHostPredications(request, existingRoute));
    existingRoute.setRouteQueryPredications(createQueryPredications(request, existingRoute));
    existingRoute.setRouteRemoteAddrPredications(
        createRemoteAddrPredications(request, existingRoute));
    existingRoute.setRouteWeightPredications(createWeightPredications(request, existingRoute));
    existingRoute.setRouteXForwardedRemoteAddrPredications(
        createXForwardedRemoteAddrPredications(request, existingRoute));
  }

  private RouteDefinition createRouteDefinition(RouteRequest request, ApiProxy apiProxy) {
    RouteDefinition routeDefinition = new RouteDefinition();
    routeDefinition.setId(request.getRouteId());
    routeDefinition.setUri(URI.create(apiProxy.getUri()));

    List<PredicateDefinition> predicates = new ArrayList<>();
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

    if (request.getCookiePredications() != null && !request.getCookiePredications().isEmpty()) {
      request
          .getCookiePredications()
          .forEach(
              cookiePredication ->
                  predicates.add(
                      definitionUtils.createPredicateDefinition(
                          COOKIE, cookiePredication.getName(), cookiePredication.getRegexp())));
    }

    if (request.getHeaderPredications() != null && !request.getHeaderPredications().isEmpty()) {
      request
          .getHeaderPredications()
          .forEach(
              headerPredication ->
                  predicates.add(
                      definitionUtils.createPredicateDefinition(
                          HEADER, headerPredication.getName(), headerPredication.getRegexp())));
    }

    if (request.getHostPredications() != null && !request.getHostPredications().isEmpty()) {
      String hostPatterns =
          request.getHostPredications().stream()
              .map(HostPredication::getPattern)
              .collect(Collectors.joining(","));
      predicates.add(definitionUtils.createPredicateDefinition(HOST, hostPatterns));
    }

    if (request.getQueryPredications() != null && !request.getQueryPredications().isEmpty()) {
      request
          .getQueryPredications()
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

    if (request.getRemoteAddrPredications() != null
        && !request.getRemoteAddrPredications().isEmpty()) {
      String sources =
          request.getRemoteAddrPredications().stream()
              .map(RemoteAddrPredication::getSource)
              .collect(Collectors.joining(","));
      predicates.add(definitionUtils.createPredicateDefinition(REMOTE_ADDR, sources));
    }

    if (request.getWeightPredications() != null && !request.getWeightPredications().isEmpty()) {
      request
          .getWeightPredications()
          .forEach(
              weightPredication -> {
                predicates.add(
                    definitionUtils.createPredicateDefinition(
                        WEIGHT,
                        weightPredication.getGroup(),
                        weightPredication.getWeight().toString()));
              });
    }

    if (request.getXForwardedRemoteAddrPredications() != null
        && !request.getXForwardedRemoteAddrPredications().isEmpty()) {
      String sources =
          request.getXForwardedRemoteAddrPredications().stream()
              .map(XForwardedRemoteAddrPredication::getSource)
              .collect(Collectors.joining(","));
      predicates.add(definitionUtils.createPredicateDefinition(X_FORWARDED_REMOTE_ADDR, sources));
    }

    routeDefinition.setPredicates(predicates);
    List<FilterDefinition> filters = createFilterDefinitions(request);
    routeDefinition.setFilters(filters);
    return routeDefinition;
  }

  private List<FilterDefinition> createFilterDefinitions(RouteRequest request) {
    List<FilterDefinition> filters = new ArrayList<>();
    if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
      Map<FilterType, List<HeaderConfiguration>> groupedHeaders =
          request.getHeaders().stream()
              .collect(Collectors.groupingBy(HeaderConfiguration::getType));

      groupedHeaders.forEach(
          (filterType, headerConfigurations) -> {
            switch (filterType) {
              case ADD_REQUEST_HEADER:
                headerConfigurations.forEach(
                    header ->
                        filters.add(
                            definitionUtils.createHeaderFilterDefinition(
                                ADD_REQUEST_HEADER, header.getKey(), header.getValue())));
                break;
              case ADD_REQUEST_HEADER_IF_NOT_PRESENT:
                String headerArgs =
                    headerConfigurations.stream()
                        .map(header -> header.getKey() + ":" + header.getValue())
                        .collect(Collectors.joining(","));
                filters.add(
                    definitionUtils.createHeaderFilterDefinition(
                        ADD_REQUEST_HEADER_IF_NOT_PRESENT, headerArgs));
                break;
            }
          });
    }
    return filters;
  }

  private List<RouteHeaderConfiguration> createHeaderConfigurations(
      RouteRequest request, Route route) {
    if (request.getHeaders() == null) {
      return new ArrayList<>();
    }

    return request.getHeaders().stream()
        .map(
            headerConfiguration ->
                RouteHeaderConfiguration.builder()
                    .key(headerConfiguration.getKey())
                    .value(headerConfiguration.getValue())
                    .type(headerConfiguration.getType())
                    .route(route)
                    .build())
        .toList();
  }

  private List<RouteCookiePredication> createCookiePredications(RouteRequest request, Route route) {
    if (request.getCookiePredications() == null) {
      return new ArrayList<>();
    }

    return request.getCookiePredications().stream()
        .map(
            cookiePredication ->
                RouteCookiePredication.builder()
                    .name(cookiePredication.getName())
                    .regexp(cookiePredication.getRegexp())
                    .route(route)
                    .build())
        .toList();
  }

  private List<RouteHeaderPredication> createHeaderPredications(RouteRequest request, Route route) {
    if (request.getHeaderPredications() == null) {
      return new ArrayList<>();
    }

    return request.getHeaderPredications().stream()
        .map(
            headerPredication ->
                RouteHeaderPredication.builder()
                    .name(headerPredication.getName())
                    .regexp(headerPredication.getRegexp())
                    .route(route)
                    .build())
        .toList();
  }

  private List<RouteHostPredication> createHostPredications(RouteRequest request, Route route) {
    if (request.getHostPredications() == null) {
      return new ArrayList<>();
    }

    return request.getHostPredications().stream()
        .map(
            hostPredication ->
                RouteHostPredication.builder()
                    .pattern(hostPredication.getPattern())
                    .route(route)
                    .build())
        .toList();
  }

  private List<RouteQueryPredication> createQueryPredications(RouteRequest request, Route route) {
    if (request.getQueryPredications() == null) {
      return new ArrayList<>();
    }

    return request.getQueryPredications().stream()
        .map(
            queryPredication ->
                RouteQueryPredication.builder()
                    .param(queryPredication.getParam())
                    .regexp(
                        queryPredication.getRegexp() != null
                            ? queryPredication.getRegexp()
                            : DEFAULT_REGEX)
                    .route(route)
                    .build())
        .toList();
  }

  private List<RouteRemoteAddrPredication> createRemoteAddrPredications(
      RouteRequest request, Route route) {
    if (request.getRemoteAddrPredications() == null) {
      return new ArrayList<>();
    }

    return request.getRemoteAddrPredications().stream()
        .map(
            remoteAddrPredication ->
                RouteRemoteAddrPredication.builder()
                    .source(remoteAddrPredication.getSource())
                    .route(route)
                    .build())
        .toList();
  }

  private List<RouteWeightPredication> createWeightPredications(RouteRequest request, Route route) {
    if (request.getWeightPredications() == null) {
      return new ArrayList<>();
    }

    return request.getWeightPredications().stream()
        .map(
            weightPredication ->
                RouteWeightPredication.builder()
                    .group(weightPredication.getGroup())
                    .weight(weightPredication.getWeight())
                    .route(route)
                    .build())
        .toList();
  }

  private List<RouteXForwardedRemoteAddrPredication> createXForwardedRemoteAddrPredications(
      RouteRequest request, Route route) {
    if (request.getXForwardedRemoteAddrPredications() == null) {
      return new ArrayList<>();
    }

    return request.getXForwardedRemoteAddrPredications().stream()
        .map(
            predication ->
                RouteXForwardedRemoteAddrPredication.builder()
                    .source(predication.getSource())
                    .route(route)
                    .build())
        .toList();
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
    List<HeaderConfiguration> headerConfigurations = new ArrayList<>();
    if (route.getRouteHeaderConfigurations() != null) {
      headerConfigurations =
          route.getRouteHeaderConfigurations().stream()
              .map(
                  headerConfig -> {
                    HeaderConfiguration headerConfiguration = new HeaderConfiguration();
                    headerConfiguration.setKey(headerConfig.getKey());
                    headerConfiguration.setValue(headerConfig.getValue());
                    headerConfiguration.setType(headerConfig.getType());
                    return headerConfiguration;
                  })
              .toList();
    }

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

    return RouteResponse.builder()
        .routeId(route.getRouteId())
        .enabled(route.isEnabled())
        .path(route.getPath())
        .method(route.getMethod())
        .matchTrailingSlash(route.isMatchTrailingSlash())
        .headers(headerConfigurations)
        .activationTime(route.getActivationTime())
        .expirationTime(route.getExpirationTime())
        .cookiePredications(cookiePredicationResponses)
        .headerPredications(headerPredicationResponses)
        .hostPredications(hostPredicationResponses)
        .queryPredications(queryPredicationResponses)
        .remoteAddrPredications(remoteAddrPredicationResponses)
        .weightPredications(weightPredicationResponses)
        .xForwardedRemoteAddrPredications(xForwardedRemoteAddrPredicationResponses)
        .build();
  }
}
