package tr.com.khg.services.gateway.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.KeyResolvers;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestRateLimiterResponse {
  private Integer replenishRate;
  private Integer burstCapacity;
  private Integer requestedTokens;
  private KeyResolvers keyResolver;
  private String headerName;
}
