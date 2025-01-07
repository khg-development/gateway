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
import tr.com.khg.services.gateway.model.request.HeaderPredication;
import tr.com.khg.services.gateway.model.request.Predications;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.service.RouteService;
import tr.com.khg.services.gateway.utils.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteHeaderPredicateTest {

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
  void whenHeaderMatchesExactValue_thenRouteShouldBeActive() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Test-Header")
                                .regexp("test-value")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("X-Test-Header", "test-value")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenHeaderDoesNotMatch_thenRouteShouldBeInactive() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Test-Header")
                                .regexp("test-value")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("X-Test-Header", "wrong-value")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenHeaderIsMissing_thenRouteShouldBeInactive() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Test-Header")
                                .regexp("test-value")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenHeaderMatchesRegexPattern_thenRouteShouldBeActive() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Version")
                                .regexp("^v\\d+\\.\\d+\\.\\d+$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).header("X-Version", "v1.2.3").exchange().expectStatus().isOk();
  }

  @Test
  void whenMultipleHeadersMatch_thenRouteShouldBeActive() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder().name("X-Version").regexp("^v\\d+").build(),
                            HeaderPredication.builder()
                                .name("X-Api-Key")
                                .regexp("[a-zA-Z0-9]{32}")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("X-Version", "v1")
        .header("X-Api-Key", "abcdef1234567890abcdef1234567890")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenSomeHeadersDoNotMatch_thenRouteShouldBeInactive() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder().name("X-Version").regexp("^v\\d+").build(),
                            HeaderPredication.builder()
                                .name("X-Api-Key")
                                .regexp("[a-zA-Z0-9]{32}")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("X-Version", "v1")
        .header("X-Api-Key", "invalid-key")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenHeaderValueIsCaseSensitive_thenShouldRespectCase() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Test-Header")
                                .regexp("TestValue")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("X-Test-Header", "TestValue")
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .get()
        .uri(path)
        .header("X-Test-Header", "testvalue")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenHeaderValueContainsSpecialCharacters_thenShouldHandleCorrectly() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Special")
                                .regexp("^[!@#$%^&*(),.?\":{}|<>]+$")
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient
        .get()
        .uri(path)
        .header("X-Special", "!@#$%^&*(),.?\":{}|<>")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenHeaderValueContainsUnicodeCharacters_thenShouldHandleCorrectly() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Unicode")
                                .regexp(".*") // Herhangi bir karakter dizisini kabul et
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    // Unicode karakterler ile test
    webTestClient
        .get()
        .uri(path)
        .header("X-Unicode", "ПРИВЕТ")
        .header("Content-Type", "application/json;charset=UTF-8")
        .header("Accept-Charset", "UTF-8")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void whenHeaderValueMatchesSimplePattern_thenShouldHandleCorrectly() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Test")
                                .regexp("value-\\d+") // Basit bir pattern
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    // Pattern ile eşleşen değer
    webTestClient.get().uri(path).header("X-Test", "value-123").exchange().expectStatus().isOk();

    // Pattern ile eşleşmeyen değer
    webTestClient
        .get()
        .uri(path)
        .header("X-Test", "invalid-value")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenHeaderValueContainsTurkishCharacters_thenShouldHandleCorrectly() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));

    // Basit bir Türkçe kelime için test
    String turkishValue = "Türkçe";

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Turkish")
                                .regexp(turkishValue) // Tam eşleşme için
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    // Birebir aynı değer ile test
    webTestClient
        .get()
        .uri(path)
        .header("X-Turkish", turkishValue)
        .header("Content-Type", "application/json;charset=UTF-8")
        .exchange()
        .expectStatus()
        .isOk();

    // Farklı bir Türkçe değer ile test (negatif senaryo)
    webTestClient
        .get()
        .uri(path)
        .header("X-Turkish", "Deneme")
        .header("Content-Type", "application/json;charset=UTF-8")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenHeaderValueContainsTurkishPattern_thenShouldHandleCorrectly() {
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
                    .headers(
                        List.of(
                            HeaderPredication.builder()
                                .name("X-Turkish")
                                .regexp("\\p{L}+") // Unicode harf karakterleri için pattern
                                .build()))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    // Türkçe karakterler ile test
    webTestClient
        .get()
        .uri(path)
        .header("X-Turkish", "Türkçe")
        .header("Content-Type", "application/json;charset=UTF-8")
        .exchange()
        .expectStatus()
        .isOk();

    // Latin karakterler ile test
    webTestClient
        .get()
        .uri(path)
        .header("X-Turkish", "Test")
        .header("Content-Type", "application/json;charset=UTF-8")
        .exchange()
        .expectStatus()
        .isOk();

    // Sayı içeren değer ile test (negatif senaryo)
    webTestClient
        .get()
        .uri(path)
        .header("X-Turkish", "Test123")
        .header("Content-Type", "application/json;charset=UTF-8")
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
