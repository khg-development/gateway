package tr.com.khg.services.gateway.web.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.model.request.RouteStatusRequest;
import tr.com.khg.services.gateway.model.response.RouteResponse;
import tr.com.khg.services.gateway.model.response.RoutesResponse;
import tr.com.khg.services.gateway.service.RouteService;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

  private final RouteService routeService;

  public RouteController(RouteService routeService) {
    this.routeService = routeService;
  }

  @GetMapping("/{proxyName}")
  public ResponseEntity<Mono<RoutesResponse>> getRoutesByProxy(@PathVariable String proxyName) {
    return ResponseEntity.ok(routeService.getRoutesByProxy(proxyName));
  }

  @PostMapping("/{proxyName}")
  public ResponseEntity<Mono<RouteResponse>> addRoute(
      @PathVariable String proxyName,
      @Valid @RequestBody RouteRequest routeRequest) {
    return ResponseEntity.ok(routeService.addRoute(proxyName, routeRequest));
  }

  @PutMapping("/{proxyName}/{routeId}")
  public ResponseEntity<Mono<RouteResponse>> updateRoute(
      @PathVariable String proxyName,
      @PathVariable String routeId,
      @Valid @RequestBody RouteRequest routeRequest) {
    return ResponseEntity.ok(routeService.updateRoute(proxyName, routeId, routeRequest));
  }

  @DeleteMapping("/{proxyName}/{routeId}")
  public ResponseEntity<Mono<RouteResponse>> deleteRoute(
      @PathVariable String proxyName,
      @PathVariable String routeId) {
    return ResponseEntity.ok(routeService.deleteRoute(proxyName, routeId));
  }

  @PostMapping("/{proxyName}/{routeId}/status")
  public ResponseEntity<Mono<RouteResponse>> updateRouteStatus(
      @PathVariable String proxyName,
      @PathVariable String routeId,
      @Valid @RequestBody RouteStatusRequest request) {
    return ResponseEntity.ok(routeService.updateRouteStatus(proxyName, routeId, request.getEnabled()));
  }
}
