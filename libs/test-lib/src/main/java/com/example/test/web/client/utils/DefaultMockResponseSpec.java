package com.example.test.web.client.utils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

/** Helper record for JSON deserialization of MockResponseSpec instances. */
record DefaultMockResponseSpec(
    @Nullable HttpMethod method,
    @Nullable URI url,
    @Nullable @JsonDeserialize(using = HttpHeadersDeserializer.class) HttpHeaders headers,
    @Nullable @JsonDeserialize(using = BodyDeserializer.class) byte[] body,
    @NotNull @Valid Response response)
    implements MockResponseSpec {

  public boolean test(HttpMethod method, URI url, HttpHeaders headers, byte[] body) {
    return checkMethod(method) && checkUrl(url) && checkHeaders(headers) && checkBody(body);
  }

  record Response(
      @NotNull HttpStatus status,
      @Nullable @JsonDeserialize(using = HttpHeadersDeserializer.class) HttpHeaders headers,
      @Nullable @JsonDeserialize(using = BodyDeserializer.class) byte[] body)
      implements MockResponseSpec.Response {}

  private boolean checkMethod(HttpMethod method) {
    return this.method == null || this.method.equals(method);
  }

  private boolean checkUrl(URI url) {
    return this.url == null || this.url.equals(url);
  }

  private boolean checkHeaders(HttpHeaders headers) {
    return this.headers == null
        || headers.isEmpty()
        || this.headers.entrySet().stream()
            .allMatch(e -> checkValues(headers, e.getKey(), e.getValue()));
  }

  private static boolean checkValues(HttpHeaders headers, String name, List<String> required) {
    return new HashSet<>(headers.getValuesAsList(name)).containsAll(required);
  }

  private boolean checkBody(byte[] body) {
    return this.body == null || Arrays.equals(this.body, body);
  }
}
