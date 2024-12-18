package tr.com.khg.services.gateway.web.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.model.response.FallbackResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/fallback")
public class FallbackController {

  @GetMapping
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public Mono<FallbackResponse> fallback() {
    log.warn("Circuit breaker triggered, returning fallback response");
    return Mono.just(
        FallbackResponse.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error("Circuit Breaker")
            .message("Service is temporarily unavailable. Please try again later.")
            .build());
  }
}
