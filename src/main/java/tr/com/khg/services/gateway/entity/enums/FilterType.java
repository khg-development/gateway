package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum FilterType {
  ADD_REQUEST_HEADER("AddRequestHeader", "name", "value"),
  ADD_REQUEST_HEADER_IF_NOT_PRESENT("AddRequestHeadersIfNotPresent", "name", "value"),
  ADD_RESPONSE_HEADER("AddResponseHeader", "name", "value");

  private final String type;
  private final String[] args;

  FilterType(String type, String... args) {
    this.type = type;
    this.args = args;
  }
}
