package tr.com.khg.services.gateway.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.Data;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

@Data
public class RouteRequest {
  @NotBlank(message = "Route ID boş olamaz")
  private String routeId;

  @NotBlank(message = "Path boş olamaz")
  private String path;

  private Boolean matchTrailingSlash = true;

  @NotNull(message = "HTTP metodu boş olamaz")
  private HttpMethods method;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime activationTime;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime expirationTime;

  private Boolean bodyLogEnabled = true;

  private Boolean preserveHostHeader = false;

  private Predications predications = new Predications();

  private Filters filters = new Filters();

  private Boolean secureHeadersEnabled = false;
}
