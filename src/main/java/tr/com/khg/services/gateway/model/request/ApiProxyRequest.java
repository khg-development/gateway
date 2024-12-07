package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiProxyRequest {
  @NotBlank(message = "Proxy adı boş olamaz")
  private String name;

  @NotBlank(message = "URI boş olamaz")
  private String uri;

  private String description;
}
