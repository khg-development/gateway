package tr.com.khg.services.gateway.service.mapper;

import org.springframework.stereotype.Component;
import tr.com.khg.services.gateway.entity.RouteQueryPredication;
import tr.com.khg.services.gateway.model.response.QueryPredicationResponse;

@Component
public class QueryPredicationMapper
    implements BaseMapper<RouteQueryPredication, QueryPredicationResponse> {

  @Override
  public RouteQueryPredication toEntity(QueryPredicationResponse dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public QueryPredicationResponse toDto(RouteQueryPredication entity) {
    if (entity == null) {
      return null;
    }
    return QueryPredicationResponse.builder()
        .param(entity.getParam())
        .regexp(entity.getRegexp())
        .build();
  }
}
