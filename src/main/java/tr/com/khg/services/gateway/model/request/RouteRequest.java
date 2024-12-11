package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;
import tr.com.khg.services.gateway.model.HeaderConfiguration;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class RouteRequest {
  @NotBlank(message = "Route ID boş olamaz")
  private String routeId;

  @NotBlank(message = "Path boş olamaz")
  private String path;

  @NotNull(message = "HTTP metodu boş olamaz")
  private HttpMethods method;

  private List<HeaderConfiguration> headers;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime activationTime;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime expirationTime;

  private List<CookieConfiguration> cookies;
}
