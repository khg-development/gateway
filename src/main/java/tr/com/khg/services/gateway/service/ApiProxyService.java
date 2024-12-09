package tr.com.khg.services.gateway.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tr.com.khg.services.gateway.entity.ApiProxy;
import tr.com.khg.services.gateway.model.request.ApiProxyRequest;
import tr.com.khg.services.gateway.model.response.ApiProxyResponse;
import tr.com.khg.services.gateway.model.response.PageResponse;
import tr.com.khg.services.gateway.repository.ApiProxyRepository;

@Service
@RequiredArgsConstructor
public class ApiProxyService {

  private final ApiProxyRepository apiProxyRepository;

  public Mono<PageResponse<ApiProxyResponse>> getAllProxies(Pageable pageable) {
    return Mono.fromCallable(
            () -> {
              Page<ApiProxy> proxyPage = apiProxyRepository.findAll(pageable);
              List<ApiProxyResponse> responses =
                  proxyPage.getContent().stream()
                      .map(this::mapToResponse)
                      .collect(Collectors.toList());

              return PageResponse.<ApiProxyResponse>builder()
                  .content(responses)
                  .totalElements(proxyPage.getTotalElements())
                  .totalPages(proxyPage.getTotalPages())
                  .currentPage(proxyPage.getNumber())
                  .size(proxyPage.getSize())
                  .hasNext(proxyPage.hasNext())
                  .hasPrevious(proxyPage.hasPrevious())
                  .build();
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<ApiProxyResponse> getProxyById(Long id) {
    return Mono.fromCallable(
            () ->
                apiProxyRepository
                    .findById(id)
                    .map(this::mapToResponse)
                    .orElseThrow(() -> new RuntimeException("Proxy not found: " + id)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Transactional
  public Mono<ApiProxyResponse> createProxy(ApiProxyRequest request) {
    return Mono.fromCallable(
            () -> {
              ApiProxy proxy =
                  ApiProxy.builder()
                      .name(request.getName())
                      .uri(request.getUri())
                      .description(request.getDescription())
                      .build();
              return mapToResponse(apiProxyRepository.save(proxy));
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Transactional
  public Mono<ApiProxyResponse> updateProxy(Long id, ApiProxyRequest request) {
    return Mono.fromCallable(
            () -> {
              ApiProxy proxy =
                  apiProxyRepository
                      .findById(id)
                      .orElseThrow(() -> new RuntimeException("Proxy not found: " + id));

              proxy.setName(request.getName());
              proxy.setUri(request.getUri());
              proxy.setDescription(request.getDescription());

              return mapToResponse(apiProxyRepository.save(proxy));
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Transactional
  public Mono<Void> deleteProxy(Long id) {
    return Mono.fromRunnable(
            () -> {
              apiProxyRepository
                  .findById(id)
                  .orElseThrow(() -> new RuntimeException("Proxy not found: " + id));
              apiProxyRepository.deleteById(id);
            })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  private ApiProxyResponse mapToResponse(ApiProxy proxy) {
    return ApiProxyResponse.builder()
        .id(proxy.getId())
        .name(proxy.getName())
        .uri(proxy.getUri())
        .description(proxy.getDescription())
        .createdAt(proxy.getCreatedAt())
        .updatedAt(proxy.getUpdatedAt())
        .build();
  }
}
