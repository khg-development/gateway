package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryPredication {
  @NotBlank(message = "Query parameter name cannot be empty")
  private String param;

  private String regexp;
}
