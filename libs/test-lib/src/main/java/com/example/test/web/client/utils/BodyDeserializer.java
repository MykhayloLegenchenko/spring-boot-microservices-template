package com.example.test.web.client.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;

/** Deserializer class that can deserialize byte[] instances. */
class BodyDeserializer extends StdDeserializer<byte[]> {
  public BodyDeserializer() {
    super(HttpHeaders.class);
  }

  @Override
  public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return p.readValueAsTree().toString().getBytes(StandardCharsets.UTF_8);
  }
}
