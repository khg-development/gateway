package tr.com.khg.services.gateway.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.HeaderConfiguration;

@Data
public class RouteRequest {
  @NotBlank(message = "Route ID boş olamaz")
  private String routeId;

  @NotBlank(message = "Path boş olamaz")
  private String path;

  private Boolean matchTrailingSlash = true;

  @NotNull(message = "HTTP metodu boş olamaz")
  private HttpMethods method;

  private List<HeaderConfiguration> headers;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime activationTime;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime expirationTime;

  private List<CookiePredication> cookiePredications;

  private List<HeaderPredication> headerPredications;

  private List<HostPredication> hostPredications;

  private List<QueryPredication> queryPredications;

  private List<RemoteAddrPredication> remoteAddrPredications;
}
