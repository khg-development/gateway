package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RouteStatusRequest {
  @NotNull(message = "Enabled değeri boş olamaz")
  private Boolean enabled;
}
