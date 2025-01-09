package tr.com.khg.services.gateway.web.rest;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.service.RouteService;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoutePathPredicateTest {

  @Autowired private WebTestClient webTestClient;
  @Autowired private RouteService routeService;
  @Autowired private ApiProxyRepository apiProxyRepository;
  @Autowired private RouteRepository routeRepository;
  @Autowired private RouteDefinitionLocator routeDefinitionLocator;
  @Autowired private RouteDefinitionWriter routeDefinitionWriter;
  @Autowired private ApplicationEventPublisher eventPublisher;

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

    eventPublisher.publishEvent(new RefreshRoutesEvent(this));

    mockServer.stop();
  }

  @Test
  void whenExactPathMatches_thenRouteShouldBeActive() {
    final String path = "/api/users";
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
  void whenPathWithSingleSegmentVariable_thenRouteShouldBeActive() {
    final String pathPattern = "/api/{segment}/users";
    final String actualPath = "/api/v1/users";

    mockServer
        .when(request().withMethod("GET").withPath(actualPath))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(pathPattern)
            .method(HttpMethods.GET)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(actualPath).exchange().expectStatus().isOk();
  }

  @Test
  void whenPathWithMultipleSegmentVariables_thenRouteShouldBeActive() {
    final String pathPattern = "/api/{version}/users/{id}";
    final String actualPath = "/api/v1/users/123";

    mockServer
        .when(request().withMethod("GET").withPath(actualPath))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(pathPattern)
            .method(HttpMethods.GET)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(actualPath).exchange().expectStatus().isOk();
  }

  @Test
  void whenPathWithWildcard_thenRouteShouldBeActive() {
    final String pathPattern = "/api/**";
    final String actualPath = "/api/v1/users/123/details";

    mockServer
        .when(request().withMethod("GET").withPath(actualPath))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(pathPattern)
            .method(HttpMethods.GET)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(actualPath).exchange().expectStatus().isOk();
  }

  @Test
  void whenPathWithTrailingSlash_thenRouteShouldBeActive() {
    final String pathPattern = "/api/users";
    final String actualPath = "/api/users/";

    mockServer
        .when(request().withMethod("GET").withPath(actualPath))
        .respond(response().withStatusCode(200));

    mockServer
        .when(request().withMethod("GET").withPath(pathPattern))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(pathPattern)
            .method(HttpMethods.GET)
            .matchTrailingSlash(true)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(actualPath).exchange().expectStatus().isOk();
    webTestClient.get().uri(pathPattern).exchange().expectStatus().isOk();
  }

  @Test
  void whenPathWithCustomRegexPattern_thenRouteShouldBeActive() {
    final String pathPattern = "/users/{name:[a-z]+}";
    final String actualPath = "/users/john";

    mockServer
        .when(request().withMethod("GET").withPath(actualPath))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(pathPattern)
            .method(HttpMethods.GET)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(actualPath).exchange().expectStatus().isOk();
    webTestClient.get().uri("/users/john123").exchange().expectStatus().isNotFound();
  }

  @Test
  void whenPathWithQueryParams_thenRouteShouldBeActive() {
    final String pathPattern = "/api/users";
    final String actualPath = "/api/users?name=john&age=30";

    mockServer
        .when(request().withMethod("GET").withPath("/api/users"))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(pathPattern)
            .method(HttpMethods.GET)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(actualPath).exchange().expectStatus().isOk();
  }

  @Test
  void whenRoutePathUpdated_thenShouldReflectChanges() {
    final String initialPath = "/api/v1/users";
    final String updatedPath = "/api/v2/users";
    final String routeId = "test-route-" + UUID.randomUUID();

    mockServer
        .when(request().withMethod("GET").withPath(initialPath))
        .respond(response().withStatusCode(200));
    mockServer
        .when(request().withMethod("GET").withPath(updatedPath))
        .respond(response().withStatusCode(200));

    RouteRequest initialRequest =
        RouteRequest.builder().routeId(routeId).path(initialPath).method(HttpMethods.GET).build();

    routeService.addRoute(testProxyName, initialRequest).block();

    webTestClient.get().uri(initialPath).exchange().expectStatus().isOk();

    RouteRequest updatedRequest =
        RouteRequest.builder().routeId(routeId).path(updatedPath).method(HttpMethods.GET).build();

    routeService.updateRoute(testProxyName, updatedRequest).block();

    webTestClient.get().uri(initialPath).exchange().expectStatus().isNotFound();
    webTestClient.get().uri(updatedPath).exchange().expectStatus().isOk();
  }

  @Test
  void whenMultipleRoutesWithDifferentPaths_thenShouldRouteCorrectly() {
    final String pathV1 = "/api/v1/users";
    final String pathV2 = "/api/v2/users";

    mockServer
        .when(request().withMethod("GET").withPath(pathV1).withHeader("Host", "localhost:8081"))
        .respond(response().withStatusCode(200).withHeader("X-Version", "v1"));

    mockServer
        .when(request().withMethod("GET").withPath(pathV2).withHeader("Host", "localhost:8081"))
        .respond(response().withStatusCode(201).withHeader("X-Version", "v2"));

    RouteRequest routeV1 =
        RouteRequest.builder()
            .routeId("route-v1-" + UUID.randomUUID())
            .path(pathV1)
            .method(HttpMethods.GET)
            .build();

    RouteRequest routeV2 =
        RouteRequest.builder()
            .routeId("route-v2-" + UUID.randomUUID())
            .path(pathV2)
            .method(HttpMethods.GET)
            .build();

    routeService.addRoute(testProxyName, routeV1).block();
    routeService.addRoute(testProxyName, routeV2).block();

    webTestClient
        .get()
        .uri(pathV1)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("X-Version", "v1");

    webTestClient
        .get()
        .uri(pathV2)
        .exchange()
        .expectStatus()
        .isEqualTo(201)
        .expectHeader()
        .valueEquals("X-Version", "v2");

    webTestClient.get().uri("/api/v3/users").exchange().expectStatus().isNotFound();
  }
}
