package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddRequestParameterFilter {
    @NotBlank(message = "Parameter name cannot be empty")
    private String name;

    @NotBlank(message = "Parameter value cannot be empty")
    private String value;
}
