package com.example.test.web.client.blocking;

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
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.client.RestClient;

/** Service for mocking {@link RestClient} responses. */
@Service
@Slf4j
public class MockRestClientService implements BeanPostProcessor, ClientHttpRequestInterceptor {
  private final List<BiPredicate<HttpRequest, byte[]>> downstream = new ArrayList<>();
  private final List<MockResponseSpec> responses = new ArrayList<>();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Map<String, String> placeholders = new HashMap<>();

  private final Validator validator;
  private final boolean mockBeans;
  private final UnaryOperator<String> placeholdersResolver;

  public MockRestClientService(
      Environment environment,
      Validator validator,
      @Value("${test.mock-restclient-beans:false}") boolean mockBeans) {

    this.validator = validator;
    this.mockBeans = mockBeans;
    placeholdersResolver = MockResponseUtils.placeholdersResolver(placeholders, environment);
  }

  /**
   * Loads mock response specifications from resources.
   *
   * @param path a relative path to a file or directory
   */
  public void loadFromResources(String path) throws BindException, IOException {
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
  public void downstream(BiPredicate<HttpRequest, byte[]> predicate) {
    downstream.add(predicate);
  }

  /**
   * Creates a mock object of the provided {@link RestClient.Builder}.
   *
   * @param builder the builder to mock
   * @return a mock object
   */
  public RestClient.Builder mock(RestClient.Builder builder) {
    return RestClientBuilderProxy.create(builder, this::clientCustomizer);
  }

  /**
   * Creates a mock object of the provided {@link RestClient}.
   *
   * @param client the client to mock
   * @return a mock object
   */
  public RestClient mock(RestClient client) {
    return mock(client.mutate()).build();
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    return mockBeans && bean instanceof RestClient.Builder builder
        ? RestClientBuilderProxy.create(builder, this::clientCustomizer)
        : bean;
  }

  /**
   * Intercepts the given {@link HttpRequest} and returns a mocked {@link ClientHttpResponse}.
   *
   * @see ClientHttpRequestInterceptor#intercept(HttpRequest, byte[], ClientHttpRequestExecution)
   */
  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    log.debug("Mock request: {} {} ", request.getMethod(), request.getURI());
    if (!request.getHeaders().isEmpty()) {
      log.debug("Headers: {}", request.getHeaders());
    }

    if (body.length > 0) {
      log.debug("Body: {} ", new String(body, StandardCharsets.UTF_8));
    }

    var response = findResponse(request, body);
    return response.isPresent() ? response.get() : defaultResponse(request, body, execution);
  }

  private void clientCustomizer(RestClient.Builder builder) {
    builder.requestInterceptors(
        t -> {
          t.remove(this);
          t.add(this);
        });
  }

  private Optional<ClientHttpResponse> findResponse(HttpRequest request, byte[] body) {
    return responses.stream()
        .filter(
            spec ->
                spec.test(
                    request.getMethod(),
                    request.getURI(),
                    request.getHeaders(),
                    body,
                    placeholdersResolver))
        .map(MockResponseSpec::response)
        .map(this::toClientResponse)
        .findFirst();
  }

  private ClientHttpResponse toClientResponse(MockResponseSpec.Response response) {
    var body = response.body(placeholdersResolver);
    var clientResponse =
        new MockClientHttpResponse(body != null ? body : new byte[0], response.status());

    var headers = response.headers(placeholdersResolver);
    if (headers != null) {
      clientResponse.getHeaders().addAll(headers);
    }

    return clientResponse;
  }

  private ClientHttpResponse defaultResponse(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    if (downstream.stream().anyMatch(predicate -> predicate.test(request, body))) {
      return execution.execute(request, body);
    }

    var message = "Can not find response for " + request.getMethod() + " " + request.getURI();
    log.debug(message);

    throw new NoSuchElementException(message);
  }
}
