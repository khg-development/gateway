package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum PredicateType {
  PATH("Path"),
  METHOD("Method"),
  AFTER("After"),
  BEFORE("Before"),
  BETWEEN("Between"),
  COOKIE("Cookie"),
  HEADER("Header"),
  HOST("Host"),
  QUERY("Query", "param", "regexp", "required"),
  REMOTE_ADDR("RemoteAddr", "sources"),
  WEIGHT("Weight", "weight.group", "weight.weight"),
  X_FORWARDED_REMOTE_ADDR("XForwardedRemoteAddr", "sources");

  private final String type;
  private final String[] args;

  PredicateType(String type, String... args) {
    this.type = type;
    this.args = args;
  }
}
