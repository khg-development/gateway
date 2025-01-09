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
import org.springframework.http.HttpMethod;
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
class RouteMethodPredicateTest {

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
  void whenGetMethodMatchesRoute_thenShouldBeActive() {
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
  void whenPostMethodMatchesRoute_thenShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("POST").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.POST)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.post().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenPutMethodMatchesRoute_thenShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("PUT").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.PUT)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.put().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenDeleteMethodMatchesRoute_thenShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("DELETE").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.DELETE)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.delete().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenPatchMethodMatchesRoute_thenShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("PATCH").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.PATCH)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.patch().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenHeadMethodMatchesRoute_thenShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("HEAD").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.HEAD)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.head().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenOptionsMethodMatchesRoute_thenShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("OPTIONS").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.OPTIONS)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.options().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenTraceMethodMatchesRoute_thenShouldBeActive() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("TRACE").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.TRACE)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.method(HttpMethod.TRACE).uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenRequestMethodDoesNotMatch_thenRouteShouldBeInactive() {
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

    webTestClient.post().uri(path).exchange().expectStatus().isNotFound();
    webTestClient.put().uri(path).exchange().expectStatus().isNotFound();
    webTestClient.delete().uri(path).exchange().expectStatus().isNotFound();
  }

  @Test
  void whenRouteMethodUpdated_thenShouldReflectChanges() {
    final String path = TestUtils.createMockPath();
    final String routeId = "test-route-" + UUID.randomUUID();

    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));
    mockServer
        .when(request().withMethod("POST").withPath(path))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder().routeId(routeId).path(path).method(HttpMethods.GET).build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isOk();
    webTestClient.post().uri(path).exchange().expectStatus().isNotFound();

    RouteRequest updatedRequest =
        RouteRequest.builder().routeId(routeId).path(path).method(HttpMethods.POST).build();

    routeService.updateRoute(testProxyName, updatedRequest).block();

    webTestClient.get().uri(path).exchange().expectStatus().isNotFound();
    webTestClient.post().uri(path).exchange().expectStatus().isOk();
  }

  @Test
  void whenMultipleRoutesWithDifferentMethods_thenShouldRouteCorrectly() {
    final String path = TestUtils.createMockPath();
    mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(response().withStatusCode(200));
    mockServer
        .when(request().withMethod("POST").withPath(path))
        .respond(response().withStatusCode(201));

    RouteRequest getRoute =
        RouteRequest.builder()
            .routeId("get-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .build();

    RouteRequest postRoute =
        RouteRequest.builder()
            .routeId("post-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.POST)
            .build();

    routeService.addRoute(testProxyName, getRoute).block();
    routeService.addRoute(testProxyName, postRoute).block();

    webTestClient.get().uri(path).exchange().expectStatus().isOk();
    webTestClient.post().uri(path).exchange().expectStatus().isEqualTo(201);
    webTestClient.put().uri(path).exchange().expectStatus().isNotFound();
  }
}
