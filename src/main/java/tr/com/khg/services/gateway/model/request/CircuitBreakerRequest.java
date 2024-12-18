package tr.com.khg.services.gateway.model.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerRequest {
  private String name;
  private String fallbackUri;
  private List<String> statusCodes;
}
