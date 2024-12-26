package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.StripVersionMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewriteLocationResponseHeaderRequest {
  private StripVersionMode stripVersionMode = StripVersionMode.AS_IN_REQUEST;
  private String locationHeaderName = "Location";
  private String hostValue;
  private String protocolsRegex = "https?|ftps?";
}
