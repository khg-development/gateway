package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import tr.com.khg.services.gateway.entity.enums.FilterType;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

@Data
public class RouteRequest {
  @NotBlank(message = "Route ID boş olamaz")
  private String routeId;

  @NotBlank(message = "Path boş olamaz")
  private String path;

  @NotNull(message = "HTTP metodu boş olamaz")
  private HttpMethods method;

  private List<HeaderConfiguration> headers;

  @Data
  public static class HeaderConfiguration {
    private String key;
    private String value;
    private FilterType type;
  }
}
