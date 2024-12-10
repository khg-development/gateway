package tr.com.khg.services.gateway.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.HeaderConfiguration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
  private String routeId;
  private Boolean enabled;
  private String path;
  private HttpMethods method;
  private List<HeaderConfiguration> headers;
}
