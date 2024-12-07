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

  @PostMapping
  public ResponseEntity<Mono<RouteResponse>> addRoute(@Valid @RequestBody RouteRequest routeRequest) {
    return ResponseEntity.ok(routeService.addRoute(routeRequest));
  }

  @DeleteMapping("/{routeId}")
  public ResponseEntity<Mono<RouteResponse>> deleteRoute(@PathVariable String routeId) {
    return ResponseEntity.ok(routeService.deleteRoute(routeId));
  }

  @PostMapping("/{routeId}/status")
  public ResponseEntity<Mono<RouteResponse>> updateRouteStatus(
      @PathVariable String routeId, @Valid @RequestBody RouteStatusRequest request) {
    return ResponseEntity.ok(routeService.updateRouteStatus(routeId, request.getEnabled()));
  }
}
