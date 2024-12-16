package tr.com.khg.services.gateway.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.stereotype.Service;
import tr.com.khg.services.gateway.entity.enums.FilterType;
import tr.com.khg.services.gateway.entity.enums.PredicateType;

@Service
public class DefinitionUtils {
  public PredicateDefinition createPredicateDefinition(
      PredicateType predicateType, String... args) {
    if (predicateType.getArgs().length != args.length) {
      throw new IllegalArgumentException("Number of arguments does not match predicate type");
    }
    PredicateDefinition methodPredicate = new PredicateDefinition();
    methodPredicate.setName(predicateType.getType());
    for (int i = 0; i < predicateType.getArgs().length; i++) {
      methodPredicate.addArg(predicateType.getArgs()[i], args[i]);
    }
    return methodPredicate;
  }

  public PredicateDefinition createPredicateDefinition(
      PredicateType predicateType, ZonedDateTime... args) {
    String[] strArgs =
        Arrays.stream(args)
            .map(arg -> arg.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .toArray(String[]::new);
    return createPredicateDefinition(predicateType, strArgs);
  }

  public FilterDefinition createFilterDefinition(FilterType filterType, String... args) {
    FilterDefinition headerFilter = new FilterDefinition();
    headerFilter.setName(filterType.getType());
    Map<String, String> arguments = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      arguments.put("_genkey_" + i, args[i]);
    }
    headerFilter.setArgs(arguments);
    return headerFilter;
  }
}
