package tr.com.khg.services.gateway.web.rest;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.model.response.RouteResponse;
import tr.com.khg.services.gateway.model.response.RoutesResponse;
import tr.com.khg.services.gateway.service.RouteService;

@RestController
@RequestMapping("/api")
public class RouteController {

  private final RouteService routeService;

  public RouteController(RouteService routeService) {
    this.routeService = routeService;
  }

  @GetMapping("/routes")
  public ResponseEntity<Mono<RoutesResponse>> getRoutes() {
    return ResponseEntity.ok(routeService.getAllRoutes());
  }

  @PostMapping("/routes")
  public ResponseEntity<Mono<RouteResponse>> addRoute(
      @RequestBody RouteDefinition routeDefinition) {
    return ResponseEntity.ok(routeService.addRoute(routeDefinition));
  }

  @DeleteMapping("/routes/{routeId}")
  public ResponseEntity<Mono<RouteResponse>> deleteRoute(@PathVariable String routeId) {
    return ResponseEntity.ok(routeService.deleteRoute(routeId));
  }
}
