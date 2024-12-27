package tr.com.khg.services.gateway.model.response;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

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
  private ZonedDateTime activationTime;
  private ZonedDateTime expirationTime;
  private Boolean bodyLogEnabled;
  private Boolean preserveHostHeader;
  private PredicationsResponse predications;
  private FiltersResponse filters;
  private Boolean secureHeadersEnabled;
  private Boolean saveSessionEnabled;
}
