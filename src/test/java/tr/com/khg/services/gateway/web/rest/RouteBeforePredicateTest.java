package tr.com.khg.services.gateway.web.rest;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.service.RouteService;
import tr.com.khg.services.gateway.utils.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteBeforePredicateTest {

  @Autowired private WebTestClient webTestClient;

  @Autowired private RouteService routeService;

  @Autowired private ApiProxyRepository apiProxyRepository;

  @Autowired private RouteRepository routeRepository;

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
    mockServer.stop();
  }

  @Test
  void whenRouteExpirationTimeIsFuture_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .expirationTime(ZonedDateTime.now().plusHours(1))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isOk();
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
            .expirationTime(ZonedDateTime.now().minusHours(1))
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenWaitUntilExpirationTime_thenRouteShouldBecomeInactive() throws InterruptedException {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
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
  void whenRouteExpirationTimeIsExactlyNow_thenRouteShouldBeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .expirationTime(ZonedDateTime.now())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenRouteExpirationTimeIsNull_thenRouteShouldBeActive() {
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
  void whenMultipleRoutesWithDifferentExpirationTimes_thenShouldHandleCorrectly() {
    final String activePath = TestUtils.createMockPath();
    final String inactivePath = TestUtils.createMockPath();

    mockServer
        .when(request().withMethod("GET").withPath(activePath))
        .respond(response().withStatusCode(200));
    mockServer
        .when(request().withMethod("GET").withPath(inactivePath))
        .respond(response().withStatusCode(200));

    // Aktif route oluştur
    RouteRequest activeRoute =
        RouteRequest.builder()
            .routeId("test-route-active-" + UUID.randomUUID())
            .path(activePath)
            .method(HttpMethods.GET)
            .expirationTime(ZonedDateTime.now().plusHours(1))
            .build();

    // İnaktif route oluştur
    RouteRequest inactiveRoute =
        RouteRequest.builder()
            .routeId("test-route-inactive-" + UUID.randomUUID())
            .path(inactivePath)
            .method(HttpMethods.GET)
            .expirationTime(ZonedDateTime.now().minusHours(1))
            .build();

    routeService.addRoute(testProxyName, activeRoute).block();
    routeService.addRoute(testProxyName, inactiveRoute).block();

    // Aktif route kontrolü
    webTestClient.get().uri(activePath).exchange().expectStatus().isOk();

    // İnaktif route kontrolü
    webTestClient.get().uri(inactivePath).exchange().expectStatus().isNotFound();
  }
}
