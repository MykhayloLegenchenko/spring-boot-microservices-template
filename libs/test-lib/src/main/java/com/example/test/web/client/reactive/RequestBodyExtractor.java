package com.example.test.web.client.reactive;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Body extractor for ClientRequest class. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class RequestBodyExtractor {
  public static Mono<byte[]> extract(ClientRequest request) {
    var mockRequest = new MockRequest(request.method(), request.url());
    var strategies = ExchangeStrategies.withDefaults();

    return request
        .body()
        .insert(
            mockRequest,
            new BodyInserter.Context() {
              @Override
              public List<HttpMessageWriter<?>> messageWriters() {
                return strategies.messageWriters();
              }

              @Override
              public Optional<ServerHttpRequest> serverRequest() {
                return Optional.empty();
              }

              @Override
              public Map<String, Object> hints() {
                return Map.of();
              }
            })
        .then(Mono.fromCallable(mockRequest::bodyBytes));
  }

  private static class MockRequest extends MockClientHttpRequest {
    @Nullable private DataBuffer bodyBuffer = null;

    public MockRequest(HttpMethod httpMethod, URI url) {
      super(httpMethod, url);
    }

    public byte[] bodyBytes() {
      if (bodyBuffer == null) {
        return new byte[0];
      }

      var bytes = new byte[bodyBuffer.readableByteCount()];
      bodyBuffer.read(bytes);
      DataBufferUtils.release(bodyBuffer);
      bodyBuffer = null;

      return bytes;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
      return DataBufferUtils.join(body)
          .doOnNext(
              buffer -> {
                if (bodyBuffer == null) {
                  bodyBuffer = buffer;
                } else {
                  bodyBuffer.factory().join(List.of(bodyBuffer, buffer));
                }
              })
          .then();
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
      return Flux.from(body).flatMap(this::writeWith).then();
    }
  }
}
