package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FallbackHeadersRequest {
  private String executionExceptionTypeHeaderName = "Execution-Exception-Type";
  private String executionExceptionMessageHeaderName = "Execution-Exception-Message";
  private String rootCauseExceptionTypeHeaderName = "Root-Cause-Exception-Type";
  private String rootCauseExceptionMessageHeaderName = "Root-Cause-Exception-Message";
}
