package com.example.common.web.client.reactive;

import com.example.common.error.exception.ClientResponseException;
import com.example.common.web.service.invoker.RequestParamObjectArgumentResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

/** Implementation class for {@link ReactiveClientFactory} */
final class ReactiveClientFactoryImpl implements ReactiveClientFactory {
  private static final ParameterizedTypeReference<?> defaultErrorType =
      new ParameterizedTypeReference<Map<String, Object>>() {};

  private final WebClient.Builder builder;
  private final List<Consumer<HttpServiceProxyFactory.Builder>> factoryCustomizers;

  ReactiveClientFactoryImpl(WebClient.Builder builder) {
    this.builder = builder.clone();
    factoryCustomizers = new ArrayList<>();
  }

  private ReactiveClientFactoryImpl(ReactiveClientFactoryImpl src) {
    this.builder = src.builder.clone();
    factoryCustomizers = new ArrayList<>(src.factoryCustomizers);
  }

  @Override
  public ReactiveClientFactory builder(Consumer<WebClient.Builder> configurer) {
    configurer.accept(builder);
    return this;
  }

  @Override
  public ReactiveClientFactory bearerAuth(Supplier<String> tokenSupplier) {
    builder.filter(
        (request, next) -> {
          request.headers().add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSupplier.get());
          return next.exchange(request);
        });

    return this;
  }

  @Override
  public ReactiveClientFactory errorType(ParameterizedTypeReference<?> errorType) {
    builder.filter(errorFilter(errorType));
    return this;
  }

  @Override
  public ReactiveClientFactory defaultErrorType() {
    return errorType(defaultErrorType);
  }

  @Override
  public ReactiveClientFactory factoryCustomizers(
      Consumer<List<Consumer<HttpServiceProxyFactory.Builder>>> configurer) {

    configurer.accept(factoryCustomizers);
    return this;
  }

  @Override
  public ReactiveClientFactory duplicate() {
    return new ReactiveClientFactoryImpl(this);
  }

  @Override
  public <T> T createClient(Class<T> serviceType) {
    return createFactory().createClient(serviceType);
  }

  private HttpServiceProxyFactory createFactory() {
    var factoryBuilder =
        HttpServiceProxyFactory.builderFor(WebClientAdapter.create(builder.build()));

    for (var customizer : factoryCustomizers) {
      customizer.accept(factoryBuilder);
    }

    factoryBuilder.customArgumentResolver(new RequestParamObjectArgumentResolver());

    return factoryBuilder.build();
  }

  private static ExchangeFilterFunction errorFilter(ParameterizedTypeReference<?> errorType) {
    return ExchangeFilterFunction.ofResponseProcessor(
        response -> {
          var status = response.statusCode();
          if (!status.is4xxClientError() && !status.is5xxServerError()) {
            return Mono.just(response);
          }

          return response
              .bodyToMono(errorType)
              .flatMap(error -> Mono.error(new ClientResponseException(status, error)))
              .thenReturn(response);
        });
  }
}
