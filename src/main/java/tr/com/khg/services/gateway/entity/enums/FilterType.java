package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum FilterType {
  ADD_REQUEST_HEADER("AddRequestHeader"),
  ADD_REQUEST_HEADER_IF_NOT_PRESENT("AddRequestHeadersIfNotPresent"),
  ADD_REQUEST_PARAMETER("AddRequestParameter"),
  ADD_RESPONSE_HEADER("AddResponseHeader"),
  CIRCUIT_BREAKER("CircuitBreaker");

  private final String type;

  FilterType(String type) {
    this.type = type;
  }
}
