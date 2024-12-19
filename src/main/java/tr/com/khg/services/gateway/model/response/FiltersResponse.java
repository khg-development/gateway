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
  private List<JsonToGrpcResponse> jsonToGrpc;
}
