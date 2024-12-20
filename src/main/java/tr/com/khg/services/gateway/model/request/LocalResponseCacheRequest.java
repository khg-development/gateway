package tr.com.khg.services.gateway.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.khg.services.gateway.entity.enums.NoCacheStrategy;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalResponseCacheRequest {
    private String size;
    private String timeToLive = "5m";
    private NoCacheStrategy noCacheStrategy = NoCacheStrategy.SKIP_UPDATE_CACHE_ENTRY;
}
