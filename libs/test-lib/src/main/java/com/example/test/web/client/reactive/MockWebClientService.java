package com.example.test.web.client.reactive;

import com.example.test.web.client.utils.MockResponseSpec;
import com.example.test.web.client.utils.MockResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Service for mocking {@code WebClient} responses. */
@Service
@Slf4j
@RequiredArgsConstructor
public class MockWebClientService implements BeanPostProcessor {
  private final List<BiPredicate<ClientRequest, byte[]>> downstream = new ArrayList<>();
  private final List<MockResponseSpec> responses = new ArrayList<>();
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final Validator validator;

  /**
   * Loads mock response specifications from resources.
   *
   * @param path the absolute path within the class path to a file or directory
   */
  public MockWebClientService loadFromResources(String path) throws IOException, BindException {
    responses.addAll(MockResponseUtils.responseSpecsFromResources(path, objectMapper, validator));
    return this;
  }

  /**
   * Adds downstream predicate.
   *
   * @param predicate the downstream predicate
   */
  public MockWebClientService downstream(BiPredicate<ClientRequest, byte[]> predicate) {
    downstream.add(predicate);
    return this;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    return bean instanceof WebClient.Builder builder
        ? BuilderProxy.create(builder, this::clientCustomizer)
        : bean;
  }

  private void clientCustomizer(WebClient.Builder builder) {
    builder.filters(
        t -> {
          t.remove((ExchangeFilterFunction) this::exchangeFilter);
          t.add(this::exchangeFilter);
        });
  }

  private Mono<ClientResponse> exchangeFilter(ClientRequest request, ExchangeFunction next) {
    log.debug("Mock request: {} {} ", request.method(), request.url());
    if (!request.headers().isEmpty()) {
      log.debug("Headers: {}", request.headers());
    }

    return RequestBodyExtractor.extract(request).flatMap(body -> findResponse(request, next, body));
  }

  private Mono<ClientResponse> findResponse(
      ClientRequest request, ExchangeFunction next, byte[] body) {

    if (body.length > 0) {
      log.debug("Body: {} ", new String(body, StandardCharsets.UTF_8));
    }

    return responses.stream()
        .filter(spec -> spec.test(request.method(), request.url(), request.headers(), body))
        .findFirst()
        .map(MockResponseSpec::response)
        .map(this::toClientResponse)
        .map(Mono::just)
        .orElseGet(() -> defaultResponse(request, body, next));
  }

  private ClientResponse toClientResponse(MockResponseSpec.Response response) {
    var builder = ClientResponse.create(response.status());

    var responseHeaders = response.headers();
    if (responseHeaders != null) {
      builder.headers(t -> t.addAll(responseHeaders));
    }

    var responseBody = response.body();
    if (responseBody != null && responseBody.length > 0) {
      builder.body(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(responseBody)));
    }

    return builder.build();
  }

  private Mono<ClientResponse> defaultResponse(
      ClientRequest request, byte[] body, ExchangeFunction next) {

    if (downstream.stream().anyMatch(predicate -> predicate.test(request, body))) {
      return next.exchange(request);
    }

    return Mono.error(
        new NoSuchElementException(
            "Can not find response for " + request.method() + " " + request.url()));
  }
}
