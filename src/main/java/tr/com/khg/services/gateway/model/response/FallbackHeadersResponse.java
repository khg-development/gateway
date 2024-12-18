package tr.com.khg.services.gateway.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FallbackHeadersResponse {
  private String executionExceptionTypeHeaderName;
  private String executionExceptionMessageHeaderName;
  private String rootCauseExceptionTypeHeaderName;
  private String rootCauseExceptionMessageHeaderName;
}
