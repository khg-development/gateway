package tr.com.khg.services.gateway.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.model.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Mono<ErrorResponse>> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    String message = ex.getMessage();
    String errorMessage;

    if (message.contains("uk_route_id_api_proxy_id")) {
      errorMessage = "Bu route ID zaten bu proxy için kullanılmaktadır.";
    } else if (message.contains("uk_api_proxy_path_method")) {
      errorMessage = "Bu path ve HTTP metodu kombinasyonu zaten bu proxy için tanımlanmıştır.";
    } else {
      errorMessage = "Veritabanı kısıtlaması ihlal edildi.";
    }

    return ResponseEntity.badRequest().body(Mono.just(new ErrorResponse(errorMessage)));
  }

  @ExceptionHandler(DuplicateRouteException.class)
  public ResponseEntity<Mono<ErrorResponse>> handleDuplicateRoute(DuplicateRouteException ex) {
    return ResponseEntity.badRequest().body(Mono.just(new ErrorResponse(ex.getMessage())));
  }
}
