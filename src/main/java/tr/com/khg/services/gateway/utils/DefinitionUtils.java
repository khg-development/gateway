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
    PredicateDefinition methodPredicate = new PredicateDefinition();
    methodPredicate.setName(predicateType.getType());
    Map<String, String> arguments = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      String argName = "_genkey_" + i;
      arguments.put(argName, args[i]);
    }
    methodPredicate.setArgs(arguments);
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

  public FilterDefinition createFilterDefinition(FilterType filterType, Object... args) {
    String[] strArgs = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
    return createFilterDefinition(filterType, strArgs);
  }

  public FilterDefinition createFilterDefinition(FilterType filterType, String... args) {
    FilterDefinition headerFilter = new FilterDefinition();
    headerFilter.setName(filterType.getType());
    Map<String, String> arguments = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      if (args[i] == null) {
        continue;
      }
      String argName = filterType.getArgs().length > i ? filterType.getArgs()[i] : "_genkey_" + i;
      arguments.put(argName, args[i]);
    }
    headerFilter.setArgs(arguments);
    return headerFilter;
  }
}
