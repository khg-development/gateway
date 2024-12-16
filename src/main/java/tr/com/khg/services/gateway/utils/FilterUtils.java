package tr.com.khg.services.gateway.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.Route;
import tr.com.khg.services.gateway.entity.RouteAddRequestHeaderFilter;
import tr.com.khg.services.gateway.model.request.Filters;

@Component
@RequiredArgsConstructor
public class FilterUtils {

  public List<RouteAddRequestHeaderFilter> createAddRequestHeaderFilters(
      Filters filters, Route route) {
    if (filters == null || filters.getAddRequestHeaders() == null) {
      return new ArrayList<>();
    }

    return filters.getAddRequestHeaders().stream()
        .map(
            filter ->
                RouteAddRequestHeaderFilter.builder()
                    .name(filter.getName())
                    .value(filter.getValue())
                    .route(route)
                    .build())
        .toList();
  }
}
