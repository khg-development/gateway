package tr.com.khg.services.gateway.exception;

public class CircuitBreakerNotFoundException extends RuntimeException {
    public CircuitBreakerNotFoundException(String message) {
        super(message);
    }
}
