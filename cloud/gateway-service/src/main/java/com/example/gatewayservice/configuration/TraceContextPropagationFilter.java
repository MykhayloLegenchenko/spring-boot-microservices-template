package com.example.gatewayservice.configuration;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Trace context propagation filter. */
class TraceContextPropagationFilter implements GlobalFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    var request = exchange.getRequest().mutate();
    GlobalOpenTelemetry.getPropagators()
        .getTextMapPropagator()
        .inject(
            Context.current(),
            request,
            (carrier, key, value) -> {
              assert carrier != null;
              carrier.header(key, value);
            });

    return chain.filter(exchange.mutate().request(request.build()).build());
  }
}
