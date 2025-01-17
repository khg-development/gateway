package tr.com.khg.services.gateway.model.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filters {
  private List<AddRequestHeaderFilter> addRequestHeaders;
  private List<AddRequestHeaderIfNotPresentFilterRequest> addRequestHeadersIfNotPresent;
  private List<AddRequestParameterFilter> addRequestParameters;
  private List<AddResponseHeaderFilter> addResponseHeaders;
  private List<CircuitBreakerRequest> circuitBreakers;
  private List<DedupeResponseHeaderRequest> dedupeResponseHeaders;
  private List<FallbackHeadersRequest> fallbackHeaders;
  private List<LocalResponseCacheRequest> localResponseCache;
  private List<MapRequestHeaderRequest> mapRequestHeaders;
  private List<PrefixPathRequest> prefixPaths;
  private List<RedirectToRequest> redirects;
  private List<RemoveJsonAttributesResponseBodyRequest> removeJsonAttributesResponseBody;
  private List<RemoveRequestHeaderRequest> removeRequestHeaders;
  private List<RemoveRequestParameterRequest> removeRequestParameters;
  private List<RemoveResponseHeaderRequest> removeResponseHeaders;
  private List<RequestHeaderSizeRequest> requestHeaderSizes;
  private RequestRateLimiterRequest requestRateLimiter;
  private List<RewriteLocationResponseHeaderRequest> rewriteLocationResponseHeaders;
  private List<RewritePathRequest> rewritePaths;
  private List<RewriteRequestParameterRequest> rewriteRequestParameters;
  private List<RewriteResponseHeaderRequest> rewriteResponseHeaders;
  private SetPathRequest setPath;
  private List<SetRequestHeaderRequest> setRequestHeaders;
  private List<SetResponseHeaderRequest> setResponseHeaders;
  private SetStatusRequest setStatus;
  private StripPrefixRequest stripPrefix;
  private RetryRequest retry;
  private RequestSizeRequest requestSize;
  private SetRequestHostHeaderRequest setRequestHostHeader;
}
