package com.example.common.web.client.blocking;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** The factory interface for blocking HTTP clients. */
public interface BlockingClientFactory {
  /**
   * Configure the {@link RestClient.Builder}.
   *
   * @param configurer configurer to apply
   * @return this factory
   */
  BlockingClientFactory builder(Consumer<RestClient.Builder> configurer);

  /**
   * Enable bearer authentication.
   *
   * @param tokenSupplier authorization token supplier
   * @return this factory
   */
  BlockingClientFactory bearerAuth(Supplier<String> tokenSupplier);

  /**
   * Configure the error type for the ClientResponseException to be thrown when the response status
   * is 4xx or 5xx.
   *
   * @param errorType error type reference.
   * @return this factory
   */
  BlockingClientFactory errorType(ParameterizedTypeReference<?> errorType);

  /**
   * Configure {@code Map<String, Object>} type for the ClientResponseException to be thrown when
   * the response status is 4xx or 5xx.
   *
   * @return this factory
   */
  BlockingClientFactory defaultErrorType();

  /**
   * Configure the list of {@link HttpServiceProxyFactory.Builder} customizers.
   *
   * @param configurer configurer to apply
   * @return this factory
   */
  BlockingClientFactory factoryCustomizers(
      Consumer<List<Consumer<HttpServiceProxyFactory.Builder>>> configurer);

  /** Clone this {@code BlockingClientFactory}. */
  BlockingClientFactory duplicate();

  /** Creates a new blocking HTTP client. */
  <T> T createClient(Class<T> serviceType);

  /** Creates a new {@code BlockingClientFactory}. */
  static BlockingClientFactory create(RestClient.Builder builder) {
    return new BlockingClientFactoryImpl(builder);
  }
}
