package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddRequestHeaderIfNotPresentFilterRequest {
  @NotBlank(message = "Header name cannot be empty")
  private String name;

  @NotBlank(message = "Header value cannot be empty")
  private String value;
}
