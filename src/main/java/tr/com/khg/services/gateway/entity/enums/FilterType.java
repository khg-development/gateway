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
  REDIRECT_TO("RedirectTo", "status", "url", "includeRequestParams"),
  REMOVE_JSON_ATTRIBUTES_RESPONSE_BODY(
      "RemoveJsonAttributesResponseBody", "attributes", "recursive"),
  REMOVE_REQUEST_HEADER("RemoveRequestHeader", "name"),
  REMOVE_REQUEST_PARAMETER("RemoveRequestParameter", "name"),
  REMOVE_RESPONSE_HEADER("RemoveResponseHeader", "name"),
  REQUEST_HEADER_SIZE("RequestHeaderSize", "maxSize", "errorHeaderName"),
  REQUEST_RATE_LIMITER(
      "RequestRateLimiter",
      "redis-rate-limiter.replenishRate",
      "redis-rate-limiter.burstCapacity",
      "redis-rate-limiter.requestedTokens",
      "key-resolver"),
  REWRITE_LOCATION_RESPONSE_HEADER(
      "RewriteLocationResponseHeader",
      "stripVersionMode",
      "locationHeaderName",
      "hostValue",
      "protocolsRegex"),
  REWRITE_PATH("RewritePath", "regexp", "replacement"),
  REWRITE_REQUEST_PARAMETER("RewriteRequestParameter", "name", "replacement");

  private final String type;
  private final String[] args;

  FilterType(String type, String... args) {
    this.type = type;
    this.args = args;
  }
}
