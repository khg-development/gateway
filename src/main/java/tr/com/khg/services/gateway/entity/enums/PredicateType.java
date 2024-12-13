package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum PredicateType {
  PATH("Path", "pattern", "matchTrailingSlash"),
  METHOD("Method", "method"),
  AFTER("After", "datetime"),
  BEFORE("Before", "datetime"),
  BETWEEN("Between", "datetime1", "datetime2"),
  COOKIE("Cookie", "name", "regexp"),
  HEADER("Header", "header", "regexp"),
  HOST("Host", "patterns"),
  QUERY("Query", "param", "regexp", "required"),
  REMOTE_ADDR("RemoteAddr", "sources");

  private final String type;
  private final String[] args;

  PredicateType(String type, String... args) {
    this.type = type;
    this.args = args;
  }
}
