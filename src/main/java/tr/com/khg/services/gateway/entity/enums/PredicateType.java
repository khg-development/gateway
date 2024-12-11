package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum PredicateType {
  PATH("Path", "pattern"),
  METHOD("Method", "method"),
  AFTER("After", "datetime"),
  BEFORE("Before", "datetime"),
  BETWEEN("Between", "datetime1", "datetime2");

  private final String type;
  private final String[] arg;

  PredicateType(String type, String... arg) {
    this.type = type;
    this.arg = arg;
  }
}
