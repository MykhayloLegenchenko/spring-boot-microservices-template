package com.example.common.web.client.reactive;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** The factory interface for reactive HTTP clients. */
public interface ReactiveClientFactory {
  /**
   * Configure the {@link WebClient.Builder}.
   *
   * @param configurer configurer to apply
   * @return this factory
   */
  ReactiveClientFactory builder(Consumer<WebClient.Builder> configurer);

  /**
   * Enables bearer authentication.
   *
   * @param tokenSupplier authorization token supplier
   * @return this factory
   */
  ReactiveClientFactory bearerAuth(Supplier<String> tokenSupplier);

  /**
   * Configure the error type for the ClientResponseException to be thrown when the response status
   * is 4xx or 5xx.
   *
   * @param errorType error type reference.
   * @return this factory
   */
  ReactiveClientFactory errorType(ParameterizedTypeReference<?> errorType);

  /**
   * Configure {@code Map<String, Object>} type for the ClientResponseException to be thrown when
   * the response status is 4xx or 5xx.
   *
   * @return this factory
   */
  ReactiveClientFactory defaultErrorType();

  /**
   * Configure the list of {@link HttpServiceProxyFactory.Builder} customizers.
   *
   * @param configurer configurer to apply
   * @return this factory
   */
  ReactiveClientFactory factoryCustomizers(
      Consumer<List<Consumer<HttpServiceProxyFactory.Builder>>> configurer);

  /** Clone this {@code ReactiveClientFactory}. */
  ReactiveClientFactory duplicate();

  /** Creates a new reactive HTTP client. */
  <T> T createClient(Class<T> serviceType);

  /** Creates a new {@code ReactiveClientFactory}. */
  static ReactiveClientFactory create(WebClient.Builder builder) {
    return new ReactiveClientFactoryImpl(builder);
  }
}
