package tr.com.khg.services.gateway.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.*;
import tr.com.khg.services.gateway.model.request.Predications;

@Component
@RequiredArgsConstructor
public class PredicationUtils {

  private static final String DEFAULT_REGEX = "^.+$";

  public List<RouteCookiePredication> createCookiePredications(Predications p, Route route) {
    if (p.getCookies() == null) {
      return new ArrayList<>();
    }

    return p.getCookies().stream()
        .map(
            cookiePredication ->
                RouteCookiePredication.builder()
                    .name(cookiePredication.getName())
                    .regexp(cookiePredication.getRegexp())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteHeaderPredication> createHeaderPredications(Predications p, Route route) {
    if (p.getHeaders() == null) {
      return new ArrayList<>();
    }

    return p.getHeaders().stream()
        .map(
            headerPredication ->
                RouteHeaderPredication.builder()
                    .name(headerPredication.getName())
                    .regexp(headerPredication.getRegexp())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteHostPredication> createHostPredications(Predications p, Route route) {
    if (p.getHosts() == null) {
      return new ArrayList<>();
    }

    return p.getHosts().stream()
        .map(
            hostPredication ->
                RouteHostPredication.builder()
                    .pattern(hostPredication.getPattern())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteQueryPredication> createQueryPredications(Predications p, Route route) {
    if (p.getQueries() == null) {
      return new ArrayList<>();
    }

    return p.getQueries().stream()
        .map(
            queryPredication ->
                RouteQueryPredication.builder()
                    .param(queryPredication.getParam())
                    .regexp(
                        queryPredication.getRegexp() != null
                            ? queryPredication.getRegexp()
                            : DEFAULT_REGEX)
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteRemoteAddrPredication> createRemoteAddrPredications(
      Predications p, Route route) {
    if (p.getRemoteAddresses() == null) {
      return new ArrayList<>();
    }

    return p.getRemoteAddresses().stream()
        .map(
            remoteAddrPredication ->
                RouteRemoteAddrPredication.builder()
                    .source(remoteAddrPredication.getSource())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteWeightPredication> createWeightPredications(Predications p, Route route) {
    if (p.getWeights() == null) {
      return new ArrayList<>();
    }

    return p.getWeights().stream()
        .map(
            weightPredication ->
                RouteWeightPredication.builder()
                    .group(weightPredication.getGroup())
                    .weight(weightPredication.getWeight())
                    .route(route)
                    .build())
        .toList();
  }

  public List<RouteXForwardedRemoteAddrPredication> createXForwardedRemoteAddrPredications(
      Predications p, Route route) {
    if (p.getXforwardedRemoteAddresses() == null) {
      return new ArrayList<>();
    }

    return p.getXforwardedRemoteAddresses().stream()
        .map(
            predication ->
                RouteXForwardedRemoteAddrPredication.builder()
                    .source(predication.getSource())
                    .route(route)
                    .build())
        .toList();
  }
}
