package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedirectToRequest {
  private Integer status = 302;
  private String url;
  private boolean includeRequestParams = false;
}
