package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeightPredication {
    @NotBlank(message = "Group name cannot be empty")
    private String group;

    @NotNull(message = "Weight cannot be null")
    @Min(value = 1, message = "Weight must be greater than 0")
    private Integer weight;
}
