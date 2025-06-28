package com.example.test.web.client.utils;

import java.net.URI;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** Mock HTTP response specification. */
public interface MockResponseSpec {

  /**
   * Checks if the request matches this response.
   *
   * @param method the request method
   * @param url the request URL
   * @param headers the request headers
   * @param body the request body
   * @param placeholdersResolver function that may resolve placeholders in request url, header
   *     values, and body
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  boolean test(
      HttpMethod method,
      URI url,
      HttpHeaders headers,
      byte[] body,
      UnaryOperator<String> placeholdersResolver);

  /**
   * Returns response data.
   *
   * @return the response data
   */
  Response response();

  /** Mock response data. */
  interface Response {
    /**
     * Returns response status.
     *
     * @return the response status
     */
    HttpStatus status();

    /**
     * Returns response headers.
     *
     * @param placeholdersResolver function that may resolve placeholders in a response header
     *     values
     * @return the response headers
     */
    @Nullable HttpHeaders headers(UnaryOperator<String> placeholdersResolver);

    /**
     * Returns response body.
     *
     * @param placeholdersResolver function that may resolve placeholders in a response body
     * @return the response body
     */
    byte @Nullable [] body(UnaryOperator<String> placeholdersResolver);
  }
}
