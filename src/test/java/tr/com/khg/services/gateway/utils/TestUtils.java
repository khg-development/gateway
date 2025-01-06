package tr.com.khg.services.gateway.utils;

import java.util.UUID;

public class TestUtils {
  public static String createMockPath() {
    return "/test-" + UUID.randomUUID();
  }
}
