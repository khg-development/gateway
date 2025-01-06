package tr.com.khg.services.gateway.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RouteRequest {
  @NotBlank(message = "Route ID boş olamaz")
  private String routeId;

  @NotBlank(message = "Path boş olamaz")
  private String path;

  @Builder.Default
  private Boolean matchTrailingSlash = true;

  @NotNull(message = "HTTP metodu boş olamaz")
  private HttpMethods method;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime activationTime;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime expirationTime;

  @Builder.Default
  private Boolean bodyLogEnabled = true;

  @Builder.Default
  private Boolean preserveHostHeader = false;

  @Builder.Default
  private Predications predications = new Predications();

  @Builder.Default
  private Filters filters = new Filters();

  @Builder.Default
  private Boolean secureHeadersEnabled = false;

  @Builder.Default
  private Boolean saveSessionEnabled = false;
}
