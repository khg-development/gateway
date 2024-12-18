package tr.com.khg.services.gateway.web.rest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.model.request.CircuitBreakerConfigurationRequest;
import tr.com.khg.services.gateway.model.response.CircuitBreakerConfigurationResponse;
import tr.com.khg.services.gateway.service.CircuitBreakerConfigurationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/circuit-breakers")
public class CircuitBreakerConfigController {

  private final CircuitBreakerConfigurationService circuitBreakerConfigurationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<CircuitBreakerConfigurationResponse> createCircuitBreaker(
      @RequestBody CircuitBreakerConfigurationRequest request) {
    return circuitBreakerConfigurationService.createCircuitBreaker(request);
  }

  @PutMapping("/{name}")
  public Mono<CircuitBreakerConfigurationResponse> updateCircuitBreaker(
      @PathVariable String name, @RequestBody CircuitBreakerConfigurationRequest request) {
    return circuitBreakerConfigurationService.updateCircuitBreaker(name, request);
  }

  @GetMapping
  public Mono<List<CircuitBreakerConfigurationResponse>> getAllCircuitBreakers() {
    return circuitBreakerConfigurationService.getAllCircuitBreakers();
  }

  @GetMapping("/{name}")
  public Mono<CircuitBreakerConfigurationResponse> getCircuitBreaker(@PathVariable String name) {
    return circuitBreakerConfigurationService.getCircuitBreaker(name);
  }

  @DeleteMapping("/{name}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteCircuitBreaker(@PathVariable String name) {
    return circuitBreakerConfigurationService.deleteCircuitBreaker(name);
  }
}
