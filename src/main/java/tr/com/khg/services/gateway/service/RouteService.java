package tr.com.khg.services.gateway.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.model.response.RouteResponse;
import tr.com.khg.services.gateway.model.response.RoutesResponse;
import tr.com.khg.services.gateway.repository.RouteRepository;

@Service
@RequiredArgsConstructor
public class RouteService {

  private final RouteRepository routeRepository;
  private final RouteDefinitionWriter routeDefinitionWriter;
  private final RouteDefinitionLocator routeDefinitionLocator;

  @PostConstruct
  public void loadRoutesFromDatabase() {
    Mono.fromCallable(routeRepository::findByDeletedAtIsNull)
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapIterable(routes -> routes)
        .flatMap(route -> routeDefinitionWriter.save(Mono.just(route.getRouteDefinition())))
        .subscribe();
  }

  public Mono<RoutesResponse> getAllRoutes() {
    return routeDefinitionLocator
        .getRouteDefinitions()
        .collectList()
        .map(routes -> RoutesResponse.builder().routes(routes).build());
  }

  @Transactional
  public Mono<RouteResponse> addRoute(RouteDefinition routeDefinition) {
    return Mono.fromCallable(
            () -> {
              Route route =
                  Route.builder()
                      .routeId(routeDefinition.getId())
                      .routeDefinition(routeDefinition)
                      .build();
              return routeRepository.save(route);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .then(routeDefinitionWriter.save(Mono.just(routeDefinition)))
        .then(
            Mono.just(
                RouteResponse.builder()
                    .routeId(routeDefinition.getId())
                    .routeDefinition(routeDefinition)
                    .build()));
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
}
