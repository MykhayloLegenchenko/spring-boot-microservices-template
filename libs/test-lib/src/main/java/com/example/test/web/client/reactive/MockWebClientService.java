package com.example.test.web.client.reactive;

import com.example.test.web.client.utils.MockResponseSpec;
import com.example.test.web.client.utils.MockResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
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

/** Service for mocking {@link WebClient} responses. */
@Service
@Slf4j
public class MockWebClientService implements BeanPostProcessor, ExchangeFilterFunction {
  private final List<BiPredicate<ClientRequest, byte[]>> downstream = new ArrayList<>();
  private final List<MockResponseSpec> responses = new ArrayList<>();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Map<String, String> placeholders = new HashMap<>();

  private final Validator validator;
  private final boolean mockBeans;
  private final UnaryOperator<String> placeholdersResolver;

  public MockWebClientService(
      Environment environment,
      Validator validator,
      @Value("${test.mock-webclient-beans:false}") boolean mockBeans) {

    this.validator = validator;
    this.mockBeans = mockBeans;
    placeholdersResolver = MockResponseUtils.placeholdersResolver(placeholders, environment);
  }

  /**
   * Loads mock response specifications from resources.
   *
   * @param path a relative path to a file or directory
   */
  public void loadFromResources(String path) throws IOException, BindException {
    responses.addAll(MockResponseUtils.responseSpecsFromResources(path, objectMapper, validator));
  }

  /**
   * Adds a placeholder in the format {@code ${key}} to be used when resolving request and response
   * values.
   *
   * @param key the placeholder key (without the surrounding {@code ${...}})
   * @param value the placeholder value
   */
  public void addPlaceholder(String key, String value) {
    placeholders.put(key, value);
  }

  /**
   * Removes a previously added placeholder by its key.
   *
   * @param key the placeholder key (without the surrounding {@code ${...}})
   */
  public void removePlaceholder(String key) {
    placeholders.remove(key);
  }

  /**
   * Adds a downstream predicate.
   *
   * @param predicate a downstream predicate
   */
  public void downstream(BiPredicate<ClientRequest, byte[]> predicate) {
    downstream.add(predicate);
  }

  /**
   * Creates a mock object of the provided {@link WebClient.Builder}.
   *
   * @param builder the builder to mock
   * @return a mock object
   */
  public WebClient.Builder mock(WebClient.Builder builder) {
    return WebClientBuilderProxy.create(builder, this::clientCustomizer);
  }

  /**
   * Creates a mock object of the provided {@link WebClient}.
   *
   * @param client the client to mock
   * @return a mock object
   */
  public WebClient mock(WebClient client) {
    return mock(client.mutate()).build();
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    return mockBeans && bean instanceof WebClient.Builder builder
        ? WebClientBuilderProxy.create(builder, this::clientCustomizer)
        : bean;
  }

  /**
   * Intercepts the given {@link ClientRequest} and returns a mocked {@link ClientResponse}.
   *
   * @see ExchangeFilterFunction#filter(ClientRequest, ExchangeFunction)
   */
  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    log.debug("Mock request: {} {} ", request.method(), request.url());
    if (!request.headers().isEmpty()) {
      log.debug("Headers: {}", request.headers());
    }

    return RequestBodyExtractor.extract(request).flatMap(body -> findResponse(request, next, body));
  }

  private void clientCustomizer(WebClient.Builder builder) {
    builder.filters(
        t -> {
          t.remove(this);
          t.add(this);
        });
  }

  private Mono<ClientResponse> findResponse(
      ClientRequest request, ExchangeFunction next, byte[] body) {

    if (body.length > 0) {
      log.debug("Body: {} ", new String(body, StandardCharsets.UTF_8));
    }

    return responses.stream()
        .filter(
            spec ->
                spec.test(
                    request.method(), request.url(), request.headers(), body, placeholdersResolver))
        .findFirst()
        .map(MockResponseSpec::response)
        .map(this::toClientResponse)
        .map(Mono::just)
        .orElseGet(() -> defaultResponse(request, body, next));
  }

  private ClientResponse toClientResponse(MockResponseSpec.Response response) {
    var builder = ClientResponse.create(response.status());

    var responseHeaders = response.headers(placeholdersResolver);
    if (responseHeaders != null) {
      builder.headers(t -> t.addAll(responseHeaders));
    }

    var responseBody = response.body(placeholdersResolver);
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
