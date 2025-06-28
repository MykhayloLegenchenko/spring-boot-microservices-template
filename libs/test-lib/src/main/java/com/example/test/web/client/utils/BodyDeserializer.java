package com.example.test.web.client.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;

/** Deserializer for request and response bodies. */
class BodyDeserializer extends StdDeserializer<String> {
  public BodyDeserializer() {
    super(String.class);
  }

  @Override
  public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    var node = p.readValueAsTree();
    if (node instanceof TextNode text) {
      return text.asText();
    }

    return node.toString();
  }
}
