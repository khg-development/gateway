package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import tr.com.khg.services.gateway.entity.enums.HeaderType;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

@Data
public class RouteRequest {
  @NotBlank(message = "Route ID boş olamaz")
  private String routeId;

  @NotBlank(message = "Path boş olamaz")
  private String path;

  @NotBlank(message = "Servis adı boş olamaz")
  private String serviceName;

  @NotNull(message = "HTTP metodu boş olamaz")
  private HttpMethods method;

  private List<HeaderRequest> headers;

  @Data
  public static class HeaderRequest {
    private String key;
    private String value;
    private HeaderType type;
  }
}
