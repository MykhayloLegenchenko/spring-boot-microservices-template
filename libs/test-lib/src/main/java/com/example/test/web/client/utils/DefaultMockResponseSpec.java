package com.example.test.web.client.utils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.With;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** Default implementation of {@link MockResponseSpec}. */
record DefaultMockResponseSpec(
    @Nullable @With HttpMethod method,
    boolean placeholders,
    @Nullable @URL String url,
    @Nullable @JsonDeserialize(using = HttpHeadersDeserializer.class) HttpHeaders headers,
    @Nullable @JsonDeserialize(using = BodyDeserializer.class) String body,
    @NotNull @Valid Response response)
    implements MockResponseSpec {

  @Override
  public boolean test(
      HttpMethod method,
      URI url,
      HttpHeaders headers,
      byte[] body,
      UnaryOperator<String> placeholdersResolver) {

    return checkMethod(method)
        && checkUrl(url, placeholdersResolver)
        && checkHeaders(headers, placeholdersResolver)
        && checkBody(body, placeholdersResolver);
  }

  record Response(
      @NotNull HttpStatus status,
      @Nullable @JsonDeserialize(using = HttpHeadersDeserializer.class) HttpHeaders headers,
      @Nullable @JsonDeserialize(using = BodyDeserializer.class) String body)
      implements MockResponseSpec.Response {

    @Override
    public @Nullable HttpHeaders headers(UnaryOperator<String> placeholdersResolver) {
      return headers != null ? resolvePlaceholders(headers, placeholdersResolver) : null;
    }

    @Override
    public byte @Nullable [] body(UnaryOperator<String> placeholdersResolver) {
      return body != null
          ? placeholdersResolver.apply(body).getBytes(StandardCharsets.UTF_8)
          : null;
    }
  }

  private boolean checkMethod(HttpMethod method) {
    return this.method == null || this.method.equals(method);
  }

  private boolean checkUrl(URI url, UnaryOperator<String> placeholdersResolver) {
    return this.url == null || URI.create(placeholdersResolver.apply(this.url)).equals(url);
  }

  private boolean checkHeaders(HttpHeaders headers, UnaryOperator<String> placeholdersResolver) {
    return this.headers == null
        || resolvePlaceholders(this.headers, placeholdersResolver).entrySet().stream()
            .allMatch(e -> checkValues(headers, e.getKey(), e.getValue()));
  }

  private static boolean checkValues(HttpHeaders headers, String name, List<String> required) {
    return new HashSet<>(headers.getValuesAsList(name)).containsAll(required);
  }

  private boolean checkBody(byte[] body, UnaryOperator<String> placeholdersResolver) {
    return this.body == null
        || Arrays.equals(
            placeholdersResolver.apply(this.body).getBytes(StandardCharsets.UTF_8), body);
  }

  @SuppressWarnings("ReferenceEquality")
  private static HttpHeaders resolvePlaceholders(
      HttpHeaders headers, UnaryOperator<String> placeholdersResolver) {

    var result = headers;
    for (var entry : headers.entrySet()) {
      var values = entry.getValue();

      for (var i = 0; i < values.size(); i++) {
        var oldValue = values.get(i);
        var newValue = placeholdersResolver.apply(oldValue);

        if (!newValue.equals(oldValue)) {
          if (values == entry.getValue()) {
            if (result == headers) {
              result = new HttpHeaders(headers);
            }

            values = new ArrayList<>(values);
            result.replace(entry.getKey(), values);
          }

          values.set(i, newValue);
        }
      }
    }

    return result;
  }
}
