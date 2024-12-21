package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum FilterType {
  ADD_REQUEST_HEADER("AddRequestHeader"),
  ADD_REQUEST_HEADER_IF_NOT_PRESENT("AddRequestHeadersIfNotPresent"),
  ADD_REQUEST_PARAMETER("AddRequestParameter"),
  ADD_RESPONSE_HEADER("AddResponseHeader"),
  CIRCUIT_BREAKER("CircuitBreaker", "name", "fallbackUri", "statusCodes"),
  CACHE_REQUEST_BODY("CacheRequestBody", "bodyClass"),
  DEDUPE_RESPONSE_HEADER("DedupeResponseHeader", "name", "strategy"),
  FALLBACK_HEADERS(
      "FallbackHeaders",
      "executionExceptionTypeHeaderName",
      "executionExceptionMessageHeaderName",
      "rootCauseExceptionTypeHeaderName",
      "rootCauseExceptionMessageHeaderName"),
  LOCAL_RESPONSE_CACHE("LocalResponseCache", "size", "timeToLive"),
  MAP_REQUEST_HEADER("MapRequestHeader", "fromHeader", "toHeader"),
  PREFIX_PATH("PrefixPath", "prefix"),
  PRESERVE_HOST_HEADER("PreserveHostHeader"),
  REDIRECT_TO("RedirectTo", "status", "url", "includeRequestParams");

  private final String type;
  private final String[] args;

  FilterType(String type, String... args) {
    this.type = type;
    this.args = args;
  }
}
