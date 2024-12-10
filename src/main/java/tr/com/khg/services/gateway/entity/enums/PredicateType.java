package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum PredicateType {
  PATH("Path", "pattern"),
  METHOD("Method", "method");

  private final String type;
  private final String arg;

  PredicateType(String type, String arg) {
    this.type = type;
    this.arg = arg;
  }
}
