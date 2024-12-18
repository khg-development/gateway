package tr.com.khg.services.gateway.model.request;

import java.util.List;
import lombok.Data;

@Data
public class Filters {
  private List<AddRequestHeaderFilter> addRequestHeaders;
  private List<AddRequestHeaderIfNotPresentFilterRequest> addRequestHeadersIfNotPresent;
  private List<AddRequestParameterFilter> addRequestParameters;
  private List<AddResponseHeaderFilter> addResponseHeaders;
  private List<CircuitBreakerRequest> circuitBreakers;
}
