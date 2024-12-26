package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryRequest {
    private Integer retries;
    @Builder.Default
    private List<HttpStatus> statuses = new ArrayList<>();
    @Builder.Default
    private List<HttpMethod> methods = new ArrayList<>();
    @Builder.Default
    private List<HttpStatus.Series> series = new ArrayList<>();
    private Long firstBackoff;
    private Long maxBackoff;
    private Integer factor;
    private Boolean basedOnPreviousValue;
}
