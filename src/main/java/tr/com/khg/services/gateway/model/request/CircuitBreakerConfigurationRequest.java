package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitBreakerConfigurationRequest {
  private String name;
  private Integer failureRateThreshold = 50;
  private Integer slowCallRateThreshold = 100;
  private Long slowCallDurationThreshold = 60000L;
  private Integer permittedNumberOfCallsInHalfOpenState = 10;
  private Integer slidingWindowSize = 100;
  private Integer minimumNumberOfCalls = 100;
  private Long waitDurationInOpenState = 60000L;
  private Boolean automaticTransitionFromOpenToHalfOpenEnabled = false;
}
