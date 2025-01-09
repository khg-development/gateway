package tr.com.khg.services.gateway.web.rest;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.service.RouteService;
import tr.com.khg.services.gateway.utils.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteBetweenPredicateTest {

  @Autowired private WebTestClient webTestClient;
  @Autowired private RouteService routeService;
  @Autowired private ApiProxyRepository apiProxyRepository;
  @Autowired private RouteRepository routeRepository;
  @Autowired private RouteDefinitionLocator routeDefinitionLocator;
  @Autowired private RouteDefinitionWriter routeDefinitionWriter;

  private ClientAndServer mockServer;
  private final String testProxyName = "test-proxy";

  @BeforeEach
  void setUp() {
    apiProxyRepository.deleteByName(testProxyName);
    apiProxyRepository.save(
        ApiProxy.builder().name(testProxyName).uri("http://localhost:8081").build());
    mockServer = ClientAndServer.startClientAndServer(8081);
  }

  @AfterEach
  void tearDown() {
    routeRepository.deleteAll();
    routeDefinitionLocator
        .getRouteDefinitions()
        .flatMap(
            routeDefinition ->
                routeDefinitionWriter
                    .delete(Mono.just(routeDefinition.getId()))
                    .onErrorResume(e -> Mono.empty()))
        .blockLast();
    mockServer.stop();
  }

  @Test
  void whenRouteTimeIsBetweenStartAndEnd_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().minusHours(1))
            .expirationTime(ZonedDateTime.now().plusHours(1))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenRouteActivationTimeIsFuture_thenRouteShouldBeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().plusHours(1))
            .expirationTime(ZonedDateTime.now().plusHours(2))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenRouteExpirationTimeIsPast_thenRouteShouldBeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().minusHours(2))
            .expirationTime(ZonedDateTime.now().minusHours(1))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  @SneakyThrows
  void whenWaitUntilActivationTime_thenRouteShouldBecomeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().plusSeconds(2))
            .expirationTime(ZonedDateTime.now().plusHours(1))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    // İlk istek - route inaktif olmalı
    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();

    // 3 saniye bekle
    Thread.sleep(3000);

    // İkinci istek - route aktif olmalı
    webTestClient.get().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  @SneakyThrows
  void whenWaitUntilExpirationTime_thenRouteShouldBecomeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().minusHours(1))
            .expirationTime(ZonedDateTime.now().plusSeconds(2))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    // İlk istek - route aktif olmalı
    webTestClient.get().uri(path).exchange().expectStatus().isOk();

    // 3 saniye bekle
    Thread.sleep(3000);

    // İkinci istek - route inaktif olmalı
    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenRouteTimeIsExactlyAtActivationTime_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    ZonedDateTime now = ZonedDateTime.now();
    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .activationTime(now)
            .expirationTime(now.plusHours(1))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenRouteTimeIsExactlyAtExpirationTime_thenRouteShouldBeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    ZonedDateTime now = ZonedDateTime.now();
    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .activationTime(now.minusHours(1))
            .expirationTime(now)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenRouteActivationTimeAndExpirationTimeAreNull_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenMultipleRoutesWithDifferentTimeRanges_thenShouldHandleCorrectly() {
    final String activePath = TestUtils.createMockPath();
    final String inactivePath1 = TestUtils.createMockPath();
    final String inactivePath2 = TestUtils.createMockPath();

    mockServer
        .when(request().withMethod("GET").withPath(activePath))
        .respond(response().withStatusCode(200));
    mockServer
        .when(request().withMethod("GET").withPath(inactivePath1))
        .respond(response().withStatusCode(200));
    mockServer
        .when(request().withMethod("GET").withPath(inactivePath2))
        .respond(response().withStatusCode(200));

    // Aktif route oluştur (şu an aktif)
    RouteRequest activeRoute =
        RouteRequest.builder()
            .routeId("test-route-active-" + UUID.randomUUID())
            .path(activePath)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().minusHours(1))
            .expirationTime(ZonedDateTime.now().plusHours(1))
            .build();

    // İnaktif route oluştur (henüz başlamamış)
    RouteRequest inactiveRoute1 =
        RouteRequest.builder()
            .routeId("test-route-inactive1-" + UUID.randomUUID())
            .path(inactivePath1)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().plusHours(1))
            .expirationTime(ZonedDateTime.now().plusHours(2))
            .build();

    // İnaktif route oluştur (süresi dolmuş)
    RouteRequest inactiveRoute2 =
        RouteRequest.builder()
            .routeId("test-route-inactive2-" + UUID.randomUUID())
            .path(inactivePath2)
            .method(HttpMethods.GET)
            .activationTime(ZonedDateTime.now().minusHours(2))
            .expirationTime(ZonedDateTime.now().minusHours(1))
            .build();

    routeService.addRoute(testProxyName, activeRoute).block();
    routeService.addRoute(testProxyName, inactiveRoute1).block();
    routeService.addRoute(testProxyName, inactiveRoute2).block();

    // Aktif route kontrolü
    webTestClient.get().uri(activePath).exchange().expectStatus().isOk();

    // İnaktif route kontrolleri
    webTestClient.get().uri(inactivePath1).exchange().expectStatus().isNotFound();

    webTestClient.get().uri(inactivePath2).exchange().expectStatus().isNotFound();
  }
}
