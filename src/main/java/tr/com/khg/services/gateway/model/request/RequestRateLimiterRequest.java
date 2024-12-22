package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.KeyResolvers;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestRateLimiterRequest {
    private Integer replenishRate;
    private Integer burstCapacity;
    private Integer requestedTokens = 1;
    private KeyResolvers keyResolver = KeyResolvers.PRINCIPAL_NAME;
    private String headerName = "X-Rate-Limit-Key";
}
