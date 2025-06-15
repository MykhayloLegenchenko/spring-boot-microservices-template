package com.example.service.weather.weather.api;

import com.example.common.web.client.reactive.ReactiveClientFactory;
import com.example.service.weather.weather.api.dto.ApiCurrentWeatherResult;
import java.util.List;
import java.util.Map;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/** Weather.com API interface. */
@HttpExchange
public interface WeatherComApiClient {

  @GetExchange("/current.json")
  Mono<ApiCurrentWeatherResult> current(@RequestParam("q") String q);

  static WeatherComApiClient create(WebClient.Builder builder, String url, String apiKey) {
    var params = new MultiValueMapAdapter<>(Map.of("key", List.of(apiKey)));
    var baseUrl = UriComponentsBuilder.fromUriString(url).queryParams(params).build().toString();

    return ReactiveClientFactory.create(builder.baseUrl(baseUrl))
        .defaultErrorType()
        //        .builder(NettyClientUtils.wiretap(LogLevel.INFO, AdvancedByteBufFormat.TEXTUAL))
        .createClient(WeatherComApiClient.class);
  }
}
