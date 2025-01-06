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
import tr.com.khg.services.gateway.model.request.CookiePredication;
import tr.com.khg.services.gateway.model.request.Predications;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.service.RouteService;
import tr.com.khg.services.gateway.utils.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteCookiePredicateTest {
  @Autowired private WebTestClient webTestClient;
  @Autowired private RouteService routeService;
  @Autowired private ApiProxyRepository apiProxyRepository;
  @Autowired private RouteRepository routeRepository;

  private ClientAndServer mockServer;
  private final String testProxyName = "test-proxy";

  private static final String VALID_COOKIE_VALUE = "abc123def456ghi789jkl012mno345pq";

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
  void whenRequestHasMatchingCookie_thenRouteShouldBeActive() {
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
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("session")
                                .regexp("^[a-zA-Z0-9]{32}$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .cookie("session", VALID_COOKIE_VALUE)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenRequestHasCookieWithNonMatchingValue_thenRouteShouldBeInactive() {
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
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("session")
                                .regexp("^[a-zA-Z0-9]{32}$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .cookie("session", "invalid-session-value")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenRequestDoesNotHaveCookie_thenRouteShouldBeInactive() {
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
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("session")
                                .regexp("^[a-zA-Z0-9]{32}$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenRequestHasMultipleCookies_thenAllShouldMatch() {
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
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("session")
                                .regexp("^[a-zA-Z0-9]{32}$")
                                .build(),
                            CookiePredication.builder().name("user").regexp("^[0-9]+$").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .cookie("session", VALID_COOKIE_VALUE)
        .cookie("user", "12345")
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .get()
        .uri(path)
        .cookie("session", "abc123def456ghi789jkl012mno345pqr")
        .cookie("user", "invalid-user")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenCookieValueMatchesRegexPattern_thenRouteShouldBeActive() {
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
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("version")
                                .regexp("^v[0-9]+$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).cookie("version", "v1").exchange().expectStatus().isOk();

    webTestClient.get().uri(path).cookie("version", "v2").exchange().expectStatus().isOk();

    webTestClient
        .get()
        .uri(path)
        .cookie("version", "invalid-version")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenCookieValueCaseSensitive_thenShouldMatchExactly() {
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
                    .cookies(List.of(CookiePredication.builder().name("lang").regexp("en").build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).cookie("lang", "en").exchange().expectStatus().isOk();

    webTestClient.get().uri(path).cookie("lang", "EN").exchange().expectStatus().isNotFound();
  }

  @Test
  void whenCookieValueContainsSpecialCharacters_thenShouldHandleCorrectly() {
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
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("token")
                                .regexp("^[a-zA-Z0-9!@#$%^&*()_+=-`~]+$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).cookie("token", "abc!@#123").exchange().expectStatus().isOk();

    webTestClient
        .get()
        .uri(path)
        .cookie("token", "invalid-token")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenRouteCookiePredicationUpdated_thenShouldReflectChanges() {
    final String path = TestUtils.createMockPath();
    final String routeId = "test-route-" + UUID.randomUUID();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId(routeId)
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("session")
                                .regexp("^[a-zA-Z0-9]{32}$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .cookie("session", VALID_COOKIE_VALUE)
        .exchange()
        .expectStatus()
        .isOk();

    RouteRequest updatedRouteRequest =
        RouteRequest.builder()
            .routeId(routeId)
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .cookies(
                        List.of(
                            CookiePredication.builder()
                                .name("session")
                                .regexp("^[0-9]{32}$")
                                .build()))
                    .build())
            .build();

    routeService.updateRoute(testProxyName, updatedRouteRequest).block();

    webTestClient
        .get()
        .uri(path)
        .cookie("session", "12345678901234567890123456789012")
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .get()
        .uri(path)
        .cookie("session", "abc123def456ghi789jkl012mno345pqr")
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
