package com.example.common.web.client.reactive;

import io.netty.handler.logging.LogLevel;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

/** Netty client utils */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NettyClientUtils {

  /**
   * Returns the {@code WebClient.Builder} customizer that configures Netty {@code HttpClient}
   * logger.
   */
  public static Consumer<WebClient.Builder> wiretap(LogLevel level, AdvancedByteBufFormat format) {

    return builder -> {
      var httpClient =
          HttpClient.create().wiretap("reactor.netty.http.client.HttpClient", level, format);

      builder.clientConnector(new ReactorClientHttpConnector(httpClient));
    };
  }
}
