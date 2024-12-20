package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum NoCacheStrategy {
  SKIP_UPDATE_CACHE_ENTRY("skip-update-cache-entry"),
  UPDATE_CACHE_ENTRY("update-cache-entry");

  private final String value;

  NoCacheStrategy(String value) {
    this.value = value;
  }

  public static NoCacheStrategy fromValue(String value) {
    for (NoCacheStrategy strategy : NoCacheStrategy.values()) {
      if (strategy.value.equals(value)) {
        return strategy;
      }
    }
    return SKIP_UPDATE_CACHE_ENTRY;
  }
}
