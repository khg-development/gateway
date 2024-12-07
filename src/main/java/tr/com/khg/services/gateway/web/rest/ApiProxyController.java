package tr.com.khg.services.gateway.web.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tr.com.khg.services.gateway.model.request.ApiProxyRequest;
import tr.com.khg.services.gateway.model.response.ApiProxyResponse;
import tr.com.khg.services.gateway.service.ApiProxyService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/proxies")
public class ApiProxyController {

    private final ApiProxyService apiProxyService;

    @GetMapping
    public ResponseEntity<Mono<List<ApiProxyResponse>>> getAllProxies() {
        return ResponseEntity.ok(apiProxyService.getAllProxies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<ApiProxyResponse>> getProxyById(@PathVariable Long id) {
        return ResponseEntity.ok(apiProxyService.getProxyById(id));
    }

    @PostMapping
    public ResponseEntity<Mono<ApiProxyResponse>> createProxy(
            @Valid @RequestBody ApiProxyRequest request) {
        return ResponseEntity.ok(apiProxyService.createProxy(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mono<ApiProxyResponse>> updateProxy(
            @PathVariable Long id, 
            @Valid @RequestBody ApiProxyRequest request) {
        return ResponseEntity.ok(apiProxyService.updateProxy(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Mono<Void>> deleteProxy(@PathVariable Long id) {
        return ResponseEntity.ok(apiProxyService.deleteProxy(id));
    }
} 