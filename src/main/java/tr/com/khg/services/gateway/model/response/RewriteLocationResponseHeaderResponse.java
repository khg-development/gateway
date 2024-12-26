package tr.com.khg.services.gateway.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.StripVersionMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewriteLocationResponseHeaderResponse {
    private StripVersionMode stripVersionMode;
    private String locationHeaderName;
    private String hostValue;
    private String protocolsRegex;
}
