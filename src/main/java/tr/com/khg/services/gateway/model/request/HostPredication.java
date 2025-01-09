package tr.com.khg.services.gateway.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostPredication {
  private String pattern;
}
