package com.example.common.web.client.blocking;

import com.example.common.error.exception.ClientResponseException;
import com.example.common.util.Holder;
import com.example.common.web.service.invoker.RequestParamObjectArgumentResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** Implementation class for {@link BlockingClientFactory} interface. */
final class BlockingClientFactoryImpl implements BlockingClientFactory {
  private static final ParameterizedTypeReference<?> DEFAULT_ERROR_TYPE =
      new ParameterizedTypeReference<Map<String, Object>>() {};

  private final RestClient.Builder builder;
  private final List<Consumer<HttpServiceProxyFactory.Builder>> factoryCustomizers;

  BlockingClientFactoryImpl(RestClient.Builder builder) {
    this.builder = builder.clone();
    factoryCustomizers = new ArrayList<>();
  }

  private BlockingClientFactoryImpl(BlockingClientFactoryImpl src) {
    this.builder = src.builder.clone();
    factoryCustomizers = new ArrayList<>(src.factoryCustomizers);
  }

  @Override
  public BlockingClientFactory builder(Consumer<RestClient.Builder> configurer) {
    configurer.accept(builder);
    return this;
  }

  @Override
  public BlockingClientFactory bearerAuth(Supplier<String> tokenSupplier) {
    builder.requestInitializer(
        request ->
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSupplier.get()));
    return this;
  }

  @Override
  public BlockingClientFactory errorType(ParameterizedTypeReference<?> errorType) {
    var messageConverters = Holder.<List<HttpMessageConverter<?>>>create();
    builder.messageConverters(messageConverters);

    builder.defaultStatusHandler(
        responseErrorHandler(errorType, messageConverters.asOptional().orElseThrow()));
    return this;
  }

  @Override
  public BlockingClientFactory defaultErrorType() {
    return errorType(DEFAULT_ERROR_TYPE);
  }

  @Override
  public BlockingClientFactory factoryCustomizers(
      Consumer<List<Consumer<HttpServiceProxyFactory.Builder>>> configurer) {

    configurer.accept(factoryCustomizers);
    return this;
  }

  @Override
  public BlockingClientFactory duplicate() {
    return new BlockingClientFactoryImpl(this);
  }

  @Override
  public <T> T createClient(Class<T> serviceType) {
    return createFactory().createClient(serviceType);
  }

  private HttpServiceProxyFactory createFactory() {
    var factoryBuilder =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.build()));

    for (var customizer : factoryCustomizers) {
      customizer.accept(factoryBuilder);
    }

    factoryBuilder.customArgumentResolver(new RequestParamObjectArgumentResolver());

    return factoryBuilder.build();
  }

  private static ResponseErrorHandler responseErrorHandler(
      ParameterizedTypeReference<?> errorType, List<HttpMessageConverter<?>> messageConverters) {
    var bodyExtractor = new HttpMessageConverterExtractor<>(errorType.getType(), messageConverters);

    return new ResponseErrorHandler() {

      @Override
      public boolean hasError(ClientHttpResponse response) throws IOException {
        var status = response.getStatusCode();
        return status.is4xxClientError() || status.is5xxServerError();
      }

      @Override
      public void handleError(ClientHttpResponse response) throws IOException {
        var error = bodyExtractor.extractData(response);
        if (error != null) {
          throw new ClientResponseException(response.getStatusCode(), error);
        }
      }
    };
  }
}
