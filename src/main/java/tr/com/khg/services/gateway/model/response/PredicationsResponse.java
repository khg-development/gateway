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
public class PredicationsResponse {
  private List<CookiePredicationResponse> cookies;
  private List<HeaderPredicationResponse> headers;
  private List<HostPredicationResponse> hosts;
  private List<QueryPredicationResponse> queries;
  private List<RemoteAddrPredicationResponse> remoteAddresses;
  private List<WeightPredicationResponse> weights;
  private List<XForwardedRemoteAddrPredicationResponse> xforwardedRemoteAddresses;
}
