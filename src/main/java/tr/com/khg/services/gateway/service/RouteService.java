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
import tr.com.khg.services.gateway.entity.RouteHeaderConfiguration;
import tr.com.khg.services.gateway.entity.enums.FilterType;
import tr.com.khg.services.gateway.entity.enums.PredicateType;
import tr.com.khg.services.gateway.exception.DuplicateRouteException;
import tr.com.khg.services.gateway.model.HeaderConfiguration;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.model.response.RouteResponse;
import tr.com.khg.services.gateway.model.response.RoutesResponse;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

  private final RouteRepository routeRepository;
  private final ApiProxyRepository apiProxyRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final RouteDefinitionWriter routeDefinitionWriter;

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
            .build();

    RouteDefinition routeDefinition = createRouteDefinition(request, apiProxy);
    route.setRouteDefinition(routeDefinition);

    if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
      List<RouteHeaderConfiguration> headerConfigurations =
          createHeaderConfigurations(request, route);
      route.setRouteHeaderConfigurations(headerConfigurations);
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

    existingRoute.getRouteHeaderConfigurations().clear();
    if (request.getHeaders() != null) {
      List<RouteHeaderConfiguration> headerConfigurations =
          createHeaderConfigurations(request, existingRoute);
      existingRoute.getRouteHeaderConfigurations().addAll(headerConfigurations);
    }
  }

  private RouteDefinition createRouteDefinition(RouteRequest request, ApiProxy apiProxy) {
    RouteDefinition routeDefinition = new RouteDefinition();
    routeDefinition.setId(request.getRouteId());
    routeDefinition.setUri(URI.create(apiProxy.getUri()));

    List<PredicateDefinition> predicates = new ArrayList<>();
    predicates.add(createPredicateDefinition(PATH, request.getPath()));
    predicates.add(createPredicateDefinition(METHOD, request.getMethod().name()));
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
                            createHeaderFilterDefinition(
                                ADD_REQUEST_HEADER, header.getKey(), header.getValue())));
                break;
              case ADD_REQUEST_HEADER_IF_NOT_PRESENT:
                String headerArgs =
                    headerConfigurations.stream()
                        .map(header -> header.getKey() + ":" + header.getValue())
                        .collect(Collectors.joining(","));
                filters.add(
                    createHeaderFilterDefinition(ADD_REQUEST_HEADER_IF_NOT_PRESENT, headerArgs));
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
    List<HeaderConfiguration> headerConfigurations = route.getRouteHeaderConfigurations().stream()
        .map(headerConfig -> {
            HeaderConfiguration headerConfiguration = new HeaderConfiguration();
            headerConfiguration.setKey(headerConfig.getKey());
            headerConfiguration.setValue(headerConfig.getValue());
            headerConfiguration.setType(headerConfig.getType());
            return headerConfiguration;
        })
        .toList();

    return RouteResponse.builder()
        .routeId(route.getRouteId())
        .enabled(route.isEnabled())
        .path(route.getPath())
        .method(route.getMethod())
        .headers(headerConfigurations)
        .build();
  }

  private PredicateDefinition createPredicateDefinition(
      PredicateType predicateType, String argValue) {
    PredicateDefinition methodPredicate = new PredicateDefinition();
    methodPredicate.setName(predicateType.getType());
    methodPredicate.addArg(predicateType.getArg(), argValue);
    return methodPredicate;
  }

  private FilterDefinition createHeaderFilterDefinition(FilterType filterType, String... args) {
    FilterDefinition headerFilter = new FilterDefinition();
    headerFilter.setName(filterType.getFilterName());
    Map<String, String> arguments = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      arguments.put("_genkey_" + i, args[i]);
    }
    headerFilter.setArgs(arguments);
    return headerFilter;
  }
}
