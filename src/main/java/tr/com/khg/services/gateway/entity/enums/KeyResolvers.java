package tr.com.khg.services.gateway.entity.enums;

import lombok.Getter;

@Getter
public enum KeyResolvers {
  PRINCIPAL_NAME("#{@principalNameKeyResolver}"),
  IP("#{@ipKeyResolver}"),
  CUSTOM_HEADER("#{@headerKeyResolver}");

  private final String resolverName;

  KeyResolvers(String resolverName) {
    this.resolverName = resolverName;
  }
}
