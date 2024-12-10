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
  public Mono<RouteResponse> addRoute(RouteRequest request) {
    return Mono.fromCallable(
            () -> {
              ApiProxy apiProxy =
                  apiProxyRepository
                      .findByName(request.getServiceName())
                      .orElseThrow(
                          () ->
                              new RuntimeException(
                                  "Service not found: " + request.getServiceName()));

              routeRepository
                  .findByRouteIdAndApiProxy(request.getRouteId(), apiProxy)
                  .ifPresent(
                      r -> {
                        throw new DuplicateRouteException(
                            "Bu route ID zaten bu proxy için kullanılmaktadır: "
                                + request.getRouteId());
                      });

              routeRepository
                  .findByApiProxyAndPathAndMethod(apiProxy, request.getPath(), request.getMethod())
                  .ifPresent(
                      r -> {
                        throw new DuplicateRouteException(
                            "Bu path ve HTTP metodu kombinasyonu zaten bu proxy için tanımlanmıştır: "
                                + request.getPath()
                                + " - "
                                + request.getMethod());
                      });

              RouteDefinition routeDefinition = new RouteDefinition();
              routeDefinition.setId(request.getRouteId());
              routeDefinition.setUri(URI.create(apiProxy.getUri()));

              List<PredicateDefinition> predicates = new ArrayList<>();
              predicates.add(createPredicateDefinition(PATH, request.getPath()));
              predicates.add(createPredicateDefinition(METHOD, request.getMethod().name()));
              routeDefinition.setPredicates(predicates);

              List<FilterDefinition> filters = new ArrayList<>();
              if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                Map<FilterType, List<RouteRequest.HeaderConfiguration>> groupedHeaders =
                    request.getHeaders().stream()
                        .collect(Collectors.groupingBy(RouteRequest.HeaderConfiguration::getType));

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
                              createHeaderFilterDefinition(
                                  ADD_REQUEST_HEADER_IF_NOT_PRESENT, headerArgs));
                          break;
                      }
                    });
              }
              routeDefinition.setFilters(filters);

              Route route =
                  Route.builder()
                      .routeId(request.getRouteId())
                      .routeDefinition(routeDefinition)
                      .apiProxy(apiProxy)
                      .path(request.getPath())
                      .method(request.getMethod())
                      .enabled(true)
                      .build();

              if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                List<RouteHeaderConfiguration> routeHeaderConfigurations =
                    request.getHeaders().stream()
                        .map(
                            headerConfiguration ->
                                RouteHeaderConfiguration.builder()
                                    .key(headerConfiguration.getKey())
                                    .value(headerConfiguration.getValue())
                                    .type(headerConfiguration.getType())
                                    .route(route)
                                    .build())
                        .collect(Collectors.toList());
                route.setRouteHeaderConfigurations(routeHeaderConfigurations);
              }

              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            route ->
                routeDefinitionWriter
                    .save(Mono.just(route.getRouteDefinition()))
                    .then(
                        Mono.fromRunnable(
                            () -> eventPublisher.publishEvent(new RefreshRoutesEvent(this))))
                    .thenReturn(route))
        .map(
            route ->
                RouteResponse.builder()
                    .routeId(route.getRouteId())
                    .enabled(route.isEnabled())
                    .path(route.getPath())
                    .method(route.getMethod())
                    .build());
  }

  @Transactional
  public Mono<RouteResponse> deleteRoute(String routeId) {
    return Mono.fromCallable(
            () -> {
              Route route =
                  routeRepository
                      .findByRouteId(routeId)
                      .orElseThrow(() -> new RuntimeException("Route not found: " + routeId));
              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            route ->
                routeDefinitionWriter
                    .delete(Mono.just(routeId))
                    .then(
                        Mono.fromRunnable(
                            () -> eventPublisher.publishEvent(new RefreshRoutesEvent(this))))
                    .thenReturn(route))
        .map(route -> RouteResponse.builder().routeId(route.getRouteId()).build());
  }

  @Transactional
  public Mono<RouteResponse> updateRouteStatus(String routeId, boolean enabled) {
    return Mono.fromCallable(
            () -> {
              Route route =
                  routeRepository
                      .findByRouteId(routeId)
                      .orElseThrow(() -> new RuntimeException("Route not found: " + routeId));
              route.setEnabled(enabled);
              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            route -> {
              if (enabled) {
                return routeDefinitionWriter
                    .save(Mono.just(route.getRouteDefinition()))
                    .then(
                        Mono.fromRunnable(
                            () -> eventPublisher.publishEvent(new RefreshRoutesEvent(this))))
                    .thenReturn(route);
              } else {
                return routeDefinitionWriter
                    .delete(Mono.just(routeId))
                    .then(
                        Mono.fromRunnable(
                            () -> eventPublisher.publishEvent(new RefreshRoutesEvent(this))))
                    .thenReturn(route);
              }
            })
        .map(
            route ->
                RouteResponse.builder()
                    .routeId(route.getRouteId())
                    .enabled(route.isEnabled())
                    .path(route.getPath())
                    .method(route.getMethod())
                    .build());
  }

  public Mono<RoutesResponse> getRoutesByProxy(String proxyName) {
    return Mono.fromCallable(
            () ->
                routeRepository.findByApiProxyNameOrderById(proxyName).stream()
                    .map(
                        route ->
                            RouteResponse.builder()
                                .routeId(route.getRouteId())
                                .enabled(route.isEnabled())
                                .path(route.getPath())
                                .method(route.getMethod())
                                .build())
                    .collect(Collectors.toList()))
        .subscribeOn(Schedulers.boundedElastic())
        .map(routeResponses -> RoutesResponse.builder().routes(routeResponses).build())
        .doOnNext(
            response ->
                log.debug("Found {} routes for proxy: {}", response.getRoutes().size(), proxyName));
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
