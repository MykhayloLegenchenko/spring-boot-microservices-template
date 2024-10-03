package com.example.common.web.service.invoker;

import com.example.common.web.bind.annotation.RequestParamObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

/**
 * Argument resolver for arguments annotated with {@link RequestParamObject @RequestObjectParam}
 * annotation.
 *
 * <p>Throws {@link IllegalArgumentException} if resoling fails.
 */
public class RequestParamObjectArgumentResolver implements HttpServiceArgumentResolver {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public boolean resolve(
      @Nullable Object argument,
      MethodParameter parameter,
      HttpRequestValues.Builder requestValues) {
    var annotation = parameter.getParameterAnnotation(RequestParamObject.class);
    if (annotation == null) {
      return false;
    }

    var optional = Optional.class.equals(parameter.getParameterType());
    var required = annotation.required() && !optional;
    if (argument == null) {
      Assert.isTrue(required, () -> "Missing value for parameter " + toString(parameter));
      return true;
    }

    if (optional) {
      var opt = (Optional<?>) argument;
      if (opt.isEmpty()) {
        return true;
      }

      argument = opt.get();
    }

    var node = objectMapper.valueToTree(argument);
    if (!node.isObject()) {
      throw new IllegalArgumentException("Parameter " + toString(parameter) + " must be object");
    }

    node.fields().forEachRemaining(field -> addField(field, "", false, requestValues));
    return true;
  }

  private static String toString(MethodParameter parameter) {
    return "["
        + parameter.getParameterIndex()
        + "] in "
        + parameter.getExecutable().toGenericString();
  }

  private static void addField(
      Map.Entry<String, JsonNode> entry,
      String prefix,
      boolean allowNull,
      HttpRequestValues.Builder requestValues) {

    var name = entry.getKey();
    var value = entry.getValue();

    var paramName = prefix + name;
    if (value.isArray()) {
      var index = 0;

      for (var it = value.elements(); it.hasNext(); ) {
        var el = it.next();

        addField(Map.entry("[" + index + "]", el), paramName, true, requestValues);
        index++;
      }
    }
    if (value.isObject()) {
      value.fields().forEachRemaining(el -> addField(el, paramName + ".", false, requestValues));
    } else if (allowNull || !value.isNull()) {
      requestValues.addRequestParameter(paramName, value.asText());
    }
  }
}
