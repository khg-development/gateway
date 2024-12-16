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
import tr.com.khg.services.gateway.entity.RouteAddRequestHeaderFilter;
import tr.com.khg.services.gateway.entity.RouteCookiePredication;
import tr.com.khg.services.gateway.entity.RouteHeaderPredication;
import tr.com.khg.services.gateway.entity.RouteHostPredication;
import tr.com.khg.services.gateway.entity.RouteQueryPredication;
import tr.com.khg.services.gateway.entity.RouteRemoteAddrPredication;
import tr.com.khg.services.gateway.entity.RouteWeightPredication;
import tr.com.khg.services.gateway.entity.RouteXForwardedRemoteAddrPredication;
import tr.com.khg.services.gateway.exception.DuplicateRouteException;
import tr.com.khg.services.gateway.model.request.*;
import tr.com.khg.services.gateway.model.response.AddRequestHeaderFilterResponse;
import tr.com.khg.services.gateway.model.response.CookiePredicationResponse;
import tr.com.khg.services.gateway.model.response.FiltersResponse;
import tr.com.khg.services.gateway.model.response.HeaderPredicationResponse;
import tr.com.khg.services.gateway.model.response.HostPredicationResponse;
import tr.com.khg.services.gateway.model.response.PredicationsResponse;
import tr.com.khg.services.gateway.model.response.QueryPredicationResponse;
import tr.com.khg.services.gateway.model.response.RemoteAddrPredicationResponse;
import tr.com.khg.services.gateway.model.response.RouteResponse;
import tr.com.khg.services.gateway.model.response.RoutesResponse;
import tr.com.khg.services.gateway.model.response.WeightPredicationResponse;
import tr.com.khg.services.gateway.model.response.XForwardedRemoteAddrPredicationResponse;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteAddRequestHeaderFilterRepository;
import tr.com.khg.services.gateway.repository.RouteCookiePredicationRepository;
import tr.com.khg.services.gateway.repository.RouteHeaderPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteHostPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteQueryPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteRemoteAddrPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.repository.RouteWeightPredicationRepository;
import tr.com.khg.services.gateway.repository.RouteXForwardedRemoteAddrPredicationRepository;
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
            .matchTrailingSlash(
                request.getMatchTrailingSlash() != null ? request.getMatchTrailingSlash() : true)
            .routeCookiePredications(new ArrayList<>())
            .routeHeaderPredications(new ArrayList<>())
            .routeHostPredications(new ArrayList<>())
            .routeQueryPredications(new ArrayList<>())
            .routeRemoteAddrPredications(new ArrayList<>())
            .routeXForwardedRemoteAddrPredications(new ArrayList<>())
            .routeAddRequestHeaderFilters(new ArrayList<>())
            .build();

    RouteDefinition routeDefinition = createRouteDefinition(request, apiProxy);
    route.setRouteDefinition(routeDefinition);

    Predications p = request.getPredications();

    if (p.getCookies() != null && !p.getCookies().isEmpty()) {
      List<RouteCookiePredication> cookiePredications =
          predicationUtils.createCookiePredications(p, route);
      route.setRouteCookiePredications(cookiePredications);
    }

    if (p.getHeaders() != null && !p.getHeaders().isEmpty()) {
      List<RouteHeaderPredication> headerPredications =
          predicationUtils.createHeaderPredications(p, route);
      route.setRouteHeaderPredications(headerPredications);
    }

    if (p.getHosts() != null && !p.getHosts().isEmpty()) {
      List<RouteHostPredication> hostPredications =
          predicationUtils.createHostPredications(p, route);
      route.setRouteHostPredications(hostPredications);
    }

    if (p.getQueries() != null && !p.getQueries().isEmpty()) {
      List<RouteQueryPredication> queryPredications =
          predicationUtils.createQueryPredications(p, route);
      route.setRouteQueryPredications(queryPredications);
    }

    if (p.getRemoteAddresses() != null && !p.getRemoteAddresses().isEmpty()) {
      List<RouteRemoteAddrPredication> remoteAddrPredications =
          predicationUtils.createRemoteAddrPredications(p, route);
      route.setRouteRemoteAddrPredications(remoteAddrPredications);
    }

    if (p.getWeights() != null && !p.getWeights().isEmpty()) {
      List<RouteWeightPredication> weightPredications =
          predicationUtils.createWeightPredications(p, route);
      route.setRouteWeightPredications(weightPredications);
    }

    if (p.getXforwardedRemoteAddresses() != null && !p.getXforwardedRemoteAddresses().isEmpty()) {
      List<RouteXForwardedRemoteAddrPredication> xForwardedRemoteAddrPredications =
          predicationUtils.createXForwardedRemoteAddrPredications(p, route);
      route.setRouteXForwardedRemoteAddrPredications(xForwardedRemoteAddrPredications);
    }

    Filters f = request.getFilters();

    if (f != null && f.getAddRequestHeaders() != null) {
      List<RouteAddRequestHeaderFilter> filters = filterUtils.createAddRequestHeaderFilters(f, route);
      route.setRouteAddRequestHeaderFilters(filters);
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
  }

  private RouteDefinition createRouteDefinition(RouteRequest request, ApiProxy apiProxy) {
    RouteDefinition routeDefinition = new RouteDefinition();
    routeDefinition.setId(request.getRouteId());
    routeDefinition.setUri(URI.create(apiProxy.getUri()));
    List<PredicateDefinition> predicates = new ArrayList<>();
    List<FilterDefinition> filters = new ArrayList<>();

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

    return RouteResponse.builder()
        .routeId(route.getRouteId())
        .enabled(route.isEnabled())
        .path(route.getPath())
        .method(route.getMethod())
        .matchTrailingSlash(route.isMatchTrailingSlash())
        .activationTime(route.getActivationTime())
        .expirationTime(route.getExpirationTime())
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
        .filters(FiltersResponse.builder().addRequestHeaders(addRequestHeaderResponses).build())
        .build();
  }
}
