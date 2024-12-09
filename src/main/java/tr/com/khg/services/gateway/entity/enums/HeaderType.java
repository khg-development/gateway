package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum HeaderType {
  ADD_REQUEST_HEADER("AddRequestHeader"),
  ADD_REQUEST_HEADER_IF_NOT_PRESENT("AddRequestHeadersIfNotPresent"),
  ADD_RESPONSE_HEADER("AddResponseHeader");

  private final String filterName;

  HeaderType(String filterName) {
    this.filterName = filterName;
  }
}
