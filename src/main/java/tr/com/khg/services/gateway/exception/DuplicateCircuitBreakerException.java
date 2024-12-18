package tr.com.khg.services.gateway.exception;

public class DuplicateCircuitBreakerException extends RuntimeException {
    public DuplicateCircuitBreakerException(String message) {
        super(message);
    }
}
