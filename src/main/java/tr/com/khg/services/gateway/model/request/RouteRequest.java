package tr.com.khg.services.gateway.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class RouteRequest {
    @NotBlank(message = "Route ID boş olamaz")
    private String routeId;

    @NotBlank(message = "Path boş olamaz")
    private String path;

    @NotBlank(message = "Servis adı boş olamaz")
    private String serviceName;

    @NotNull(message = "HTTP metodu boş olamaz")
    private HttpMethod method;
}