package com.example.test.web.client.utils;

import java.net.URI;
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
   * @param headers the request header
   * @param body the request body
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  boolean test(HttpMethod method, URI url, HttpHeaders headers, byte[] body);

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
     * @return the response headers
     */
    @Nullable HttpHeaders headers();

    /**
     * Returns response body.
     *
     * @return the response body
     */
    byte @Nullable [] body();
  }
}
