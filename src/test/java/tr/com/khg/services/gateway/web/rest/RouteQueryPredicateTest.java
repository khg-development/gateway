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
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.request.Predications;
import tr.com.khg.services.gateway.model.request.QueryPredication;
import tr.com.khg.services.gateway.model.request.RouteRequest;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;
import tr.com.khg.services.gateway.repository.RouteRepository;
import tr.com.khg.services.gateway.service.RouteService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteQueryPredicateTest {

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
  void whenQueryParamMatchesExactValue_thenRouteShouldBeActive() {
    final String path = "/api/users";

    QueryPredication queryPredication = new QueryPredication();
    queryPredication.setParam("version");
    queryPredication.setRegexp("v1");

    mockServer
        .when(request().withMethod("GET").withPath(path).withQueryStringParameter("version", "v1"))
        .respond(response().withStatusCode(200));

    Predications predications = new Predications();
    predications.setQueries(List.of(queryPredication));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(predications)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path + "?version=v1").exchange().expectStatus().isOk();
  }

  @Test
  void whenMultipleQueryParamsMatch_thenRouteShouldBeActive() {
    final String path = "/api/users";

    QueryPredication nameQuery = new QueryPredication();
    nameQuery.setParam("name");
    nameQuery.setRegexp("john");

    QueryPredication ageQuery = new QueryPredication();
    ageQuery.setParam("age");
    ageQuery.setRegexp("30");

    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath(path)
                .withQueryStringParameters(
                    org.mockserver.model.Parameter.param("name", "john"),
                    org.mockserver.model.Parameter.param("age", "30")))
        .respond(response().withStatusCode(200));

    Predications predications = new Predications();
    predications.setQueries(List.of(nameQuery, ageQuery));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(predications)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path + "?name=john&age=30").exchange().expectStatus().isOk();
  }

  @Test
  void whenQueryParamWithRegexPattern_thenRouteShouldBeActive() {
    final String path = "/api/products";

    QueryPredication queryPredication = new QueryPredication();
    queryPredication.setParam("code");
    queryPredication.setRegexp("[A-Z]{3}");

    mockServer
        .when(request().withMethod("GET").withPath(path).withQueryStringParameter("code", "ABC"))
        .respond(response().withStatusCode(200));

    Predications predications = new Predications();
    predications.setQueries(List.of(queryPredication));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(predications)
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path + "?code=ABC").exchange().expectStatus().isOk();

    webTestClient.get().uri(path + "?code=123").exchange().expectStatus().isNotFound();
  }

  @Test
  void whenQueryParamWithSpecialCharacters_thenRouteShouldBeActive() {
    final String path = "/api/filter";
    final String queryParam = "filter=status:active";

    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath(path)
                .withQueryStringParameter("filter", "status:active"))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .queries(List.of(new QueryPredication("filter", "status:active")))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path + "?" + queryParam).exchange().expectStatus().isOk();
  }

  @Test
  void whenQueryParamOrderDiffers_thenRouteShouldBeActive() {
    final String path = "/api/users";
    final String differentOrder = "age=30&name=john";

    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath(path)
                .withQueryStringParameters(
                    org.mockserver.model.Parameter.param("name", "john"),
                    org.mockserver.model.Parameter.param("age", "30")))
        .respond(response().withStatusCode(200));

    RouteRequest routeRequest =
        RouteRequest.builder()
            .routeId("test-route-" + UUID.randomUUID())
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .queries(
                        List.of(
                            new QueryPredication("name", "john"),
                            new QueryPredication("age", "30")))
                    .build())
            .build();

    routeService.addRoute(testProxyName, routeRequest).block();

    webTestClient.get().uri(path + "?" + differentOrder).exchange().expectStatus().isOk();
  }

  @Test
  void whenRouteQueryUpdated_thenShouldReflectChanges() {
    final String path = "/api/users";
    final String initialQuery = "version=v1";
    final String updatedQuery = "version=v2";
    final String routeId = "test-route-" + UUID.randomUUID();

    mockServer
        .when(request().withMethod("GET").withPath(path).withQueryStringParameter("version", "v1"))
        .respond(response().withStatusCode(200));

    mockServer
        .when(request().withMethod("GET").withPath(path).withQueryStringParameter("version", "v2"))
        .respond(response().withStatusCode(200));

    RouteRequest initialRequest =
        RouteRequest.builder()
            .routeId(routeId)
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .queries(List.of(new QueryPredication("version", "v1")))
                    .build())
            .build();

    routeService.addRoute(testProxyName, initialRequest).block();

    webTestClient.get().uri(path + "?" + initialQuery).exchange().expectStatus().isOk();

    RouteRequest updatedRequest =
        RouteRequest.builder()
            .routeId(routeId)
            .path(path)
            .method(HttpMethods.GET)
            .predications(
                Predications.builder()
                    .queries(List.of(new QueryPredication("version", "v2")))
                    .build())
            .build();

    routeService.updateRoute(testProxyName, updatedRequest).block();

    webTestClient.get().uri(path + "?" + initialQuery).exchange().expectStatus().isNotFound();

    webTestClient.get().uri(path + "?" + updatedQuery).exchange().expectStatus().isOk();
  }
}
