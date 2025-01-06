package tr.com.khg.services.gateway.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import tr.com.khg.services.gateway.entity.enums.HttpMethods;

class RouteTest {

    @Test
    void whenSetActivationTime_thenAfterPredicateShouldBeEnabled() {
        // given
        ZonedDateTime activationTime = ZonedDateTime.now();

        // when
        Route route = Route.builder()
            .routeId("test-route")
            .path("/test")
            .method(HttpMethods.GET)
            .activationTime(activationTime)
            .enabled(true)
            .build();

        // then
        assertThat(route.getActivationTime()).isNotNull();
        assertThat(route.getActivationTime()).isEqualTo(activationTime);
    }

    @Test
    void whenActivationTimeIsNull_thenAfterPredicateShouldBeDisabled() {
        // given & when
        Route route = Route.builder()
            .routeId("test-route")
            .path("/test")
            .method(HttpMethods.GET)
            .enabled(true)
            .build();

        // then
        assertThat(route.getActivationTime()).isNull();
    }
}
