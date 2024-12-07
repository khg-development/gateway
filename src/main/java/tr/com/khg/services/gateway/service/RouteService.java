package tr.com.khg.services.gateway.service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.Route;
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
  private final RouteDefinitionWriter routeDefinitionWriter;
  private final ApiProxyRepository apiProxyRepository;

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

              RouteDefinition routeDefinition = new RouteDefinition();
              routeDefinition.setId(request.getRouteId());
              routeDefinition.setUri(URI.create(apiProxy.getUri()));

              PredicateDefinition pathPredicate = new PredicateDefinition();
              pathPredicate.setName("Path");
              pathPredicate.addArg("pattern", request.getPath());

              PredicateDefinition methodPredicate = new PredicateDefinition();
              methodPredicate.setName("Method");
              methodPredicate.addArg("method", request.getMethod().name());

              List<PredicateDefinition> predicates = new ArrayList<>();
              predicates.add(pathPredicate);
              predicates.add(methodPredicate);
              routeDefinition.setPredicates(predicates);

              Route route =
                  Route.builder()
                      .routeId(request.getRouteId())
                      .routeDefinition(routeDefinition)
                      .apiProxy(apiProxy)
                      .enabled(true)
                      .build();

              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            route ->
                routeDefinitionWriter.save(Mono.just(route.getRouteDefinition())).thenReturn(route))
        .map(
            route ->
                RouteResponse.builder()
                    .routeId(route.getRouteId())
                    .routeDefinition(route.getRouteDefinition())
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
        .then(routeDefinitionWriter.delete(Mono.just(routeId)))
        .then(Mono.just(RouteResponse.builder().routeId(routeId).build()));
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
              Route savedRoute = routeRepository.save(route);

              RouteDefinition routeDefinition = savedRoute.getRouteDefinition();
              routeDefinition.setEnabled(enabled);
              routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();

              return savedRoute;
            })
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            route ->
                RouteResponse.builder()
                    .routeId(route.getRouteId())
                    .routeDefinition(route.getRouteDefinition())
                    .build());
  }

  public Mono<RoutesResponse> getRoutesByProxy(String proxyName) {
    return Mono.fromCallable(
            () ->
                routeRepository.findByApiProxyName(proxyName).stream()
                    .map(Route::getRouteDefinition)
                    .collect(Collectors.toList()))
        .subscribeOn(Schedulers.boundedElastic())
        .map(routeDefinitions -> RoutesResponse.builder().routes(routeDefinitions).build())
        .doOnNext(
            response ->
                log.debug("Found {} routes for proxy: {}", response.getRoutes().size(), proxyName));
  }
}
