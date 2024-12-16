package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class XForwardedRemoteAddrPredication {
  @NotBlank(message = "Source cannot be empty")
  @Pattern(
      regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}/([0-9]|[1-2][0-9]|3[0-2])$",
      message = "Invalid Source format. Example: 192.168.1.1/24")
  private String source;
}
