package tr.com.khg.services.gateway.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeaderPredication {
  private String name;
  private String regexp;
}
