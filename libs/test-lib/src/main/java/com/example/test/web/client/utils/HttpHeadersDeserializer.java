package com.example.test.web.client.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;

/** Deserializer class that can deserialize HttpHeaders instances. */
class HttpHeadersDeserializer extends StdDeserializer<HttpHeaders> {
  public HttpHeadersDeserializer() {
    super(HttpHeaders.class);
  }

  @Override
  public HttpHeaders deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    var node = p.readValueAsTree();
    if (!node.isObject()) {
      throw new IllegalArgumentException("Invalid headers: " + node);
    }

    var headers = new HttpHeaders();
    node.fieldNames().forEachRemaining(name -> addHeaders(headers, node, name));

    return headers;
  }

  private void addHeaders(HttpHeaders headers, TreeNode node, String name) {
    var value = node.get(name);
    if (value.isArray()) {
      for (int i = 0; i < value.size(); i++) {
        headers.add(name, toString(value.get(i)));
      }
    } else {
      headers.add(name, toString(value));
    }
  }

  @Nullable
  private static String toString(TreeNode value) {
    if (value instanceof ValueNode valueNode) {
      return valueNode.isNull() ? null : valueNode.asText();
    }

    throw new IllegalArgumentException("Invalid header value: " + value);
  }
}
