package tr.com.khg.services.gateway.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tr.com.khg.services.gateway.entity.CircuitBreakerConfiguration;
import tr.com.khg.services.gateway.exception.CircuitBreakerNotFoundException;
import tr.com.khg.services.gateway.exception.DuplicateCircuitBreakerException;
import tr.com.khg.services.gateway.model.request.CircuitBreakerConfigurationRequest;
import tr.com.khg.services.gateway.model.response.CircuitBreakerConfigurationResponse;
import tr.com.khg.services.gateway.repository.CircuitBreakerConfigurationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitBreakerConfigurationService {

  private final CircuitBreakerConfigurationRepository circuitBreakerConfigurationRepository;
  private final CircuitBreakerRegistry circuitBreakerRegistry;

  @PostConstruct
  public void loadCircuitBreakersFromDatabase() {
    log.info("Loading circuit breakers from database");

    Mono.fromCallable(() -> circuitBreakerConfigurationRepository.findAll())
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapIterable(configs -> configs)
        .doOnNext(
            config ->
                log.debug(
                    "Loading circuit breaker configuration: {} - {}",
                    config.getName(),
                    config.getFailureRateThreshold()))
        .doOnNext(this::createOrUpdateRuntimeCircuitBreaker)
        .doOnComplete(() -> log.info("All circuit breakers loaded successfully"))
        .doOnError(error -> log.error("Error loading circuit breakers: {}", error.getMessage()))
        .subscribe();
  }

  @Transactional
  public Mono<CircuitBreakerConfigurationResponse> createCircuitBreaker(
      CircuitBreakerConfigurationRequest request) {
    return Mono.defer(
        () ->
            Mono.fromCallable(
                    () -> circuitBreakerConfigurationRepository.existsByName(request.getName()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(
                    exists -> {
                      if (exists) {
                        return Mono.error(
                            new DuplicateCircuitBreakerException(
                                "Circuit breaker already exists with name: " + request.getName()));
                      }
                      return Mono.just(mapToEntity(request));
                    })
                .flatMap(
                    config ->
                        Mono.fromCallable(() -> circuitBreakerConfigurationRepository.save(config))
                            .subscribeOn(Schedulers.boundedElastic()))
                .doOnNext(this::createOrUpdateRuntimeCircuitBreaker)
                .map(this::mapToResponse));
  }

  @Transactional
  public Mono<CircuitBreakerConfigurationResponse> updateCircuitBreaker(
      String name, CircuitBreakerConfigurationRequest request) {
    return Mono.defer(
        () ->
            Mono.fromCallable(() -> circuitBreakerConfigurationRepository.findByName(name))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(
                    optionalConfig ->
                        optionalConfig
                            .map(Mono::just)
                            .orElseGet(
                                () ->
                                    Mono.error(
                                        new CircuitBreakerNotFoundException(
                                            "Circuit breaker not found with name: " + name))))
                .doOnNext(config -> updateConfigFromRequest(config, request))
                .flatMap(
                    config ->
                        Mono.fromCallable(() -> circuitBreakerConfigurationRepository.save(config))
                            .subscribeOn(Schedulers.boundedElastic()))
                .doOnNext(this::createOrUpdateRuntimeCircuitBreaker)
                .map(this::mapToResponse));
  }

  @Transactional(readOnly = true)
  public Mono<List<CircuitBreakerConfigurationResponse>> getAllCircuitBreakers() {
    return Mono.defer(
        () ->
            Flux.fromIterable(circuitBreakerConfigurationRepository.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::mapToResponse)
                .collectList());
  }

  @Transactional(readOnly = true)
  public Mono<CircuitBreakerConfigurationResponse> getCircuitBreaker(String name) {
    return Mono.defer(
        () ->
            Mono.fromCallable(() -> circuitBreakerConfigurationRepository.findByName(name))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(
                    optionalConfig ->
                        optionalConfig
                            .map(config -> Mono.just(mapToResponse(config)))
                            .orElseGet(
                                () ->
                                    Mono.error(
                                        new CircuitBreakerNotFoundException(
                                            "Circuit breaker not found with name: " + name)))));
  }

  @Transactional
  public Mono<Void> deleteCircuitBreaker(String name) {
    return Mono.defer(
            () ->
                Mono.fromCallable(() -> circuitBreakerConfigurationRepository.findByName(name))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(
                        optionalConfig ->
                            optionalConfig
                                .map(Mono::just)
                                .orElseGet(
                                    () ->
                                        Mono.error(
                                            new CircuitBreakerNotFoundException(
                                                "Circuit breaker not found with name: " + name))))
                    .flatMap(
                        config ->
                            Mono.fromRunnable(
                                    () -> {
                                      circuitBreakerConfigurationRepository.delete(config);
                                      circuitBreakerRegistry.remove(name);
                                    })
                                .subscribeOn(Schedulers.boundedElastic())))
        .then();
  }

  private void createOrUpdateRuntimeCircuitBreaker(CircuitBreakerConfiguration config) {
    CircuitBreakerConfig.Builder builder =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(config.getFailureRateThreshold())
            .slowCallRateThreshold(config.getSlowCallRateThreshold())
            .slowCallDurationThreshold(Duration.ofMillis(config.getSlowCallDurationThreshold()))
            .permittedNumberOfCallsInHalfOpenState(
                config.getPermittedNumberOfCallsInHalfOpenState())
            .slidingWindowSize(config.getSlidingWindowSize())
            .minimumNumberOfCalls(config.getMinimumNumberOfCalls())
            .waitDurationInOpenState(Duration.ofMillis(config.getWaitDurationInOpenState()))
            .automaticTransitionFromOpenToHalfOpenEnabled(
                config.getAutomaticTransitionFromOpenToHalfOpenEnabled());

    CircuitBreaker circuitBreaker =
        circuitBreakerRegistry.circuitBreaker(config.getName(), builder.build());
    circuitBreaker
        .getEventPublisher()
        .onStateTransition(
            event ->
                log.info(
                    "Circuit Breaker {} state changed from {} to {}",
                    event.getCircuitBreakerName(),
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState()));
  }

  private CircuitBreakerConfiguration mapToEntity(CircuitBreakerConfigurationRequest request) {
    return CircuitBreakerConfiguration.builder()
        .name(request.getName())
        .failureRateThreshold(request.getFailureRateThreshold())
        .slowCallRateThreshold(request.getSlowCallRateThreshold())
        .slowCallDurationThreshold(request.getSlowCallDurationThreshold())
        .permittedNumberOfCallsInHalfOpenState(request.getPermittedNumberOfCallsInHalfOpenState())
        .slidingWindowSize(request.getSlidingWindowSize())
        .minimumNumberOfCalls(request.getMinimumNumberOfCalls())
        .waitDurationInOpenState(request.getWaitDurationInOpenState())
        .automaticTransitionFromOpenToHalfOpenEnabled(
            request.getAutomaticTransitionFromOpenToHalfOpenEnabled())
        .build();
  }

  private CircuitBreakerConfigurationResponse mapToResponse(CircuitBreakerConfiguration config) {
    return CircuitBreakerConfigurationResponse.builder()
        .id(config.getId())
        .name(config.getName())
        .failureRateThreshold(config.getFailureRateThreshold())
        .slowCallRateThreshold(config.getSlowCallRateThreshold())
        .slowCallDurationThreshold(config.getSlowCallDurationThreshold())
        .permittedNumberOfCallsInHalfOpenState(config.getPermittedNumberOfCallsInHalfOpenState())
        .slidingWindowSize(config.getSlidingWindowSize())
        .minimumNumberOfCalls(config.getMinimumNumberOfCalls())
        .waitDurationInOpenState(config.getWaitDurationInOpenState())
        .automaticTransitionFromOpenToHalfOpenEnabled(
            config.getAutomaticTransitionFromOpenToHalfOpenEnabled())
        .createdAt(config.getCreatedAt())
        .updatedAt(config.getUpdatedAt())
        .build();
  }

  private void updateConfigFromRequest(
      CircuitBreakerConfiguration config, CircuitBreakerConfigurationRequest request) {
    if (request.getFailureRateThreshold() != null) {
      config.setFailureRateThreshold(request.getFailureRateThreshold());
    }
    if (request.getSlowCallRateThreshold() != null) {
      config.setSlowCallRateThreshold(request.getSlowCallRateThreshold());
    }
    if (request.getSlowCallDurationThreshold() != null) {
      config.setSlowCallDurationThreshold(request.getSlowCallDurationThreshold());
    }
    if (request.getPermittedNumberOfCallsInHalfOpenState() != null) {
      config.setPermittedNumberOfCallsInHalfOpenState(
          request.getPermittedNumberOfCallsInHalfOpenState());
    }
    if (request.getSlidingWindowSize() != null) {
      config.setSlidingWindowSize(request.getSlidingWindowSize());
    }
    if (request.getMinimumNumberOfCalls() != null) {
      config.setMinimumNumberOfCalls(request.getMinimumNumberOfCalls());
    }
    if (request.getWaitDurationInOpenState() != null) {
      config.setWaitDurationInOpenState(request.getWaitDurationInOpenState());
    }
    if (request.getAutomaticTransitionFromOpenToHalfOpenEnabled() != null) {
      config.setAutomaticTransitionFromOpenToHalfOpenEnabled(
          request.getAutomaticTransitionFromOpenToHalfOpenEnabled());
    }
  }
}
