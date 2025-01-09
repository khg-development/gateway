package tr.com.khg.services.gateway.web.rest;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;
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
import tr.com.khg.services.gateway.model.request.HostPredication;
import tr.com.khg.services.gateway.model.request.Predications;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.service.RouteService;
import tr.com.khg.services.gateway.utils.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteHostPredicateTest {

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
  void whenHostMatchesExactValue_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("example.com").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).header("Host", "example.com").exchange().expectStatus().isOk();
  }

  @Test
  void whenHostDoesNotMatch_thenRouteShouldBeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("example.com").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("Host", "different.com")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenHostHeaderIsMissing_thenRouteShouldBeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("example.com").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenHostMatchesWildcardPattern_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("*.example.com").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("Host", "api.example.com")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenHostMatchesMultipleSubdomains_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("*.*.example.com").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("Host", "dev.api.example.com")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenHostMatchesWithPort_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("example.com:8080").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("Host", "example.com:8080")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenMultipleHostPatternsMatch_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(
                        List.of(
                            HostPredication.builder().pattern("api.example.com").build(),
                            HostPredication.builder().pattern("dev.example.com").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    // Test first host pattern
    webTestClient
        .get()
        .uri(path)
        .header("Host", "api.example.com")
        .exchange()
        .expectStatus()
        .isOk();

    // Test second host pattern
    webTestClient
        .get()
        .uri(path)
        .header("Host", "dev.example.com")
        .exchange()
        .expectStatus()
        .isOk();

    // Test non-matching host
    webTestClient
        .get()
        .uri(path)
        .header("Host", "test.example.com")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenHostMatchesIPv4_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("192.168.1.1").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).header("Host", "192.168.1.1").exchange().expectStatus().isOk();
  }

  @Test
  void whenHostMatchesIPv6_thenRouteShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("[2001:db8::1]").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).header("Host", "[2001:db8::1]").exchange().expectStatus().isOk();
  }

  @Test
  void whenHostValueIsEmpty_thenRouteShouldBeInactive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .hosts(List.of(HostPredication.builder().pattern("example.com").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).header("Host", "").exchange().expectStatus().isNotFound();
  }
}
