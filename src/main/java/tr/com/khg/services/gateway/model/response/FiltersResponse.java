package tr.com.khg.services.gateway.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltersResponse {
  private List<AddRequestHeaderFilterResponse> addRequestHeaders;
  private List<AddRequestHeaderIfNotPresentFilterResponse> addRequestHeadersIfNotPresent;
  private List<AddRequestParameterFilterResponse> addRequestParameters;
  private List<AddResponseHeaderFilterResponse> addResponseHeaders;
  private List<CircuitBreakerResponse> circuitBreakers;
  private List<DedupeResponseHeaderResponse> dedupeResponseHeaders;
  private List<FallbackHeadersResponse> fallbackHeaders;
  private List<LocalResponseCacheResponse> localResponseCache;
  private List<MapRequestHeaderResponse> mapRequestHeaders;
  private List<PrefixPathResponse> prefixPaths;
  private List<RedirectToResponse> redirects;
  private List<RemoveJsonAttributesResponseBodyResponse> removeJsonAttributesResponseBody;
  private List<RemoveRequestHeaderResponse> removeRequestHeaders;
  private List<RemoveRequestParameterResponse> removeRequestParameters;
  private List<RemoveResponseHeaderResponse> removeResponseHeaders;
  private List<RequestHeaderSizeResponse> requestHeaderSizes;
  private RequestRateLimiterResponse requestRateLimiter;
  private List<RewriteLocationResponseHeaderResponse> rewriteLocationResponseHeaders;
  private List<RewritePathResponse> rewritePaths;
  private List<RewriteRequestParameterResponse> rewriteRequestParameters;
  private List<RewriteResponseHeaderResponse> rewriteResponseHeaders;
  private SetPathResponse setPath;
  private List<SetRequestHeaderResponse> setRequestHeaders;
  private List<SetResponseHeaderResponse> setResponseHeaders;
  private SetStatusResponse setStatus;
  private StripPrefixResponse stripPrefix;
  private RetryResponse retry;
  private RequestSizeResponse requestSize;
  private SetRequestHostHeaderResponse setRequestHostHeader;
}
