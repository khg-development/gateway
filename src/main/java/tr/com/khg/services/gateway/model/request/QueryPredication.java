package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryPredication {
  @NotBlank(message = "Query parameter name cannot be empty")
  private String param;

  private String regexp;
}
