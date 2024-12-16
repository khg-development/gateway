package tr.com.khg.services.gateway.model.response;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.HeaderConfiguration;
import tr.com.khg.services.gateway.model.response.HeaderPredicationResponse;
import tr.com.khg.services.gateway.model.response.HostPredicationResponse;
import tr.com.khg.services.gateway.model.response.QueryPredicationResponse;
import tr.com.khg.services.gateway.model.response.RemoteAddrPredicationResponse;
import tr.com.khg.services.gateway.model.response.WeightPredicationResponse;
import tr.com.khg.services.gateway.model.response.XForwardedRemoteAddrPredicationResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
  private String routeId;
  private Boolean enabled;
  private String path;
  private Boolean matchTrailingSlash;
  private HttpMethods method;
  private List<HeaderConfiguration> headers;
  private ZonedDateTime activationTime;
  private ZonedDateTime expirationTime;
  private List<CookiePredicationResponse> cookiePredications;
  private List<HeaderPredicationResponse> headerPredications;
  private List<HostPredicationResponse> hostPredications;
  private List<QueryPredicationResponse> queryPredications;
  private List<RemoteAddrPredicationResponse> remoteAddrPredications;
  private List<WeightPredicationResponse> weightPredications;
  private List<XForwardedRemoteAddrPredicationResponse> xForwardedRemoteAddrPredications;
}
