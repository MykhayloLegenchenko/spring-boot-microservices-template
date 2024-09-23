package com.example.annotation.opentelemetty.extension.sampler;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOff;
import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;
import static java.util.function.Predicate.not;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.extern.java.Log;

/**
 * Sampler that eliminates unnecessary traces.
 *
 * <p>Configuration properties:
 *
 * <ul>
 *   <li>{@code exclude-sampler.log.enabled} whether to enable logging, default {@code false}
 *   <li>{@code exclude-sampler.server.paths} list of server path patterns to exclude, separated by
 *   <li>{@code exclude-sampler.client.hosts} list of client request host patterns to exclude,
 *       separated by {@code ;}
 *   <li>{@code exclude-sampler.client.paths} list of client request path patterns to exclude,
 *       separated by {@code ;}
 */
@Log
class ExcludeSampler implements Sampler {
  private final boolean logEnabled;
  private final Sampler root;
  private final Sampler parent;
  private final List<Pattern> serverPaths;
  private final List<Pattern> clientHosts;
  private final List<Pattern> clientPaths;

  ExcludeSampler(ConfigProperties config) {
    root = new RootSampler();
    parent =
        Sampler.parentBasedBuilder(alwaysOff())
            .setLocalParentSampled(root)
            .setRemoteParentNotSampled(alwaysOff())
            .build();

    logEnabled = config.getBoolean("exclude-sampler.log.enabled", false);
    serverPaths = getPatterns(config, "exclude-sampler.server.paths");
    clientHosts = getPatterns(config, "exclude-sampler.client.hosts");
    clientPaths = getPatterns(config, "exclude-sampler.client.paths");
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {

    return switch (spanKind) {
      case SERVER ->
          serverShouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
      case CLIENT ->
          clientShouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
      default -> root.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    };
  }

  @Override
  public String getDescription() {
    return "ExcludeSampler";
  }

  private SamplingResult serverShouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {

    var path = attributes.get(stringKey("url.path"));
    if (matches(path, serverPaths)) {
      return parent.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    return root.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  private SamplingResult clientShouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {

    var host = attributes.get(stringKey("server.address"));
    if (matches(host, clientHosts)) {
      return parent.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    var uri = getURI(attributes.get(stringKey("url.full")));
    if (uri != null && matches(uri.getPath(), clientPaths)) {
      return parent.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    var dbStatement = attributes.get(stringKey("db.statement"));
    if (dbStatement != null) {
      return parent.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    return root.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Nullable
  private URI getURI(@Nullable String url) {
    if (url == null) {
      return null;
    }

    try {
      return new URI(url);
    } catch (URISyntaxException ignored) {
      if (logEnabled) {
        log.warning("Incorrect URL: " + url);
      }
    }

    return null;
  }

  private static boolean matches(@Nullable String value, List<Pattern> patterns) {
    if (value == null) {
      return false;
    }

    for (Pattern pattern : patterns) {
      if (pattern.matcher(value).matches()) {
        return true;
      }
    }

    return false;
  }

  private static List<Pattern> getPatterns(ConfigProperties config, String propertyName) {
    var property = config.getString(propertyName);
    if (property == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(property.split(";"))
        .filter(not(String::isEmpty))
        .map(Pattern::compile)
        .toList();
  }

  private class RootSampler implements Sampler {
    @Override
    public SamplingResult shouldSample(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {

      if (logEnabled) {
        log.info(
            "span ["
                + traceId
                + ", "
                + name
                + ", "
                + spanKind
                + ", "
                + attributes
                + ", "
                + parentContext
                + "]");
      }

      return alwaysOn()
          .shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    @Override
    public String getDescription() {
      return "root";
    }
  }
}
