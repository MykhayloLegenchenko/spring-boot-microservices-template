package com.example.test.web.client.blocking;

import com.example.test.web.client.utils.MockResponseSpec;
import com.example.test.web.client.utils.MockResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.client.RestClient;

/** Service for mocking {@code RestClient} responses. */
@Service
@Slf4j
@RequiredArgsConstructor
public class MockRestClientService implements BeanPostProcessor {
  private final List<BiPredicate<HttpRequest, byte[]>> downstream = new ArrayList<>();
  private final List<MockResponseSpec> responses = new ArrayList<>();
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final Validator validator;

  /**
   * Loads mock response specifications from resources.
   *
   * @param path the absolute path within the class path to a file or directory
   */
  public MockRestClientService fromResources(String path) throws BindException, IOException {
    responses.addAll(MockResponseUtils.responseSpecsFromResources(path, objectMapper, validator));

    return this;
  }

  /**
   * Adds downstream predicate.
   *
   * @param predicate the downstream predicate
   */
  public MockRestClientService downstream(BiPredicate<HttpRequest, byte[]> predicate) {
    downstream.add(predicate);
    return this;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    return bean instanceof RestClient.Builder builder
        ? BuilderProxy.create(builder, this::clientCustomizer)
        : bean;
  }

  private void clientCustomizer(RestClient.Builder builder) {
    builder.requestInterceptors(
        t -> {
          t.remove((ClientHttpRequestInterceptor) this::requestInterceptor);
          t.add(this::requestInterceptor);
        });
  }

  private ClientHttpResponse requestInterceptor(
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

  private Optional<ClientHttpResponse> findResponse(HttpRequest request, byte[] body) {
    return responses.stream()
        .filter(
            spec -> spec.test(request.getMethod(), request.getURI(), request.getHeaders(), body))
        .map(MockResponseSpec::response)
        .map(this::toClientResponse)
        .findFirst();
  }

  private ClientHttpResponse toClientResponse(MockResponseSpec.Response response) {
    var body = response.body();
    var clientResponse =
        new MockClientHttpResponse(body != null ? body : new byte[0], response.status());

    var headers = response.headers();
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

    throw new NoSuchElementException(
        "Can not find response for " + request.getMethod() + " " + request.getURI());
  }
}
