package com.example.test.web.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;

/** Utilities for mock responses. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MockResponseUtils {

  /**
   * Loads mock response specifications from resources.
   *
   * <p>Response specifications must be stored in JSON format. See {@link DefaultMockResponseSpec}
   * for details.
   *
   * @param path an absolute path within the class path to a file or directory
   * @param objectMapper an object mapper instance
   * @param validator a validator instance
   * @return a {@link List} containing loaded specifications
   */
  public static List<MockResponseSpec> responseSpecsFromResources(
      String path, ObjectMapper objectMapper, Validator validator)
      throws IOException, BindException {

    return responseSpecsFromFile(new ClassPathResource(path).getFile(), objectMapper, validator);
  }

  /**
   * Creates a resolver function that replaces placeholders in the format {@code ${key}} using
   * values from the given map and the provided {@link Environment}.
   *
   * <p>Placeholders are resolved first from the {@code placeholders} map, and then from the {@code
   * Environment} if not found in the map.
   *
   * @param placeholders a map of placeholder keys and their corresponding values
   * @param environment the {@code Environment} to use as a fallback for resolving placeholders
   * @return a {@code UnaryOperator<String>} that resolves placeholders in input strings
   */
  public static UnaryOperator<String> placeholdersResolver(
      Map<String, String> placeholders, Environment environment) {

    var helper = new PropertyPlaceholderHelper("${", "}");
    return value ->
        helper.replacePlaceholders(
            value,
            key -> {
              var result = placeholders.get(key);
              return result != null ? result : environment.getProperty(key);
            });
  }

  private static List<MockResponseSpec> responseSpecsFromFile(
      File file, ObjectMapper objectMapper, Validator validator) throws IOException, BindException {

    var result = new ArrayList<MockResponseSpec>();
    if (file.isDirectory()) {
      var files = file.listFiles();
      if (files != null) {
        for (var child : files) {
          result.addAll(responseSpecsFromFile(child, objectMapper, validator));
        }
      }

      return result;
    }

    MockResponseSpec spec;
    try (var is = new FileInputStream(file)) {
      spec = objectMapper.readValue(is, DefaultMockResponseSpec.class);
    }

    var binder = new DataBinder(spec, DefaultMockResponseSpec.class.getSimpleName());
    binder.setValidator(validator);
    binder.validate();
    binder.close();

    result.add(spec);
    return result;
  }
}
