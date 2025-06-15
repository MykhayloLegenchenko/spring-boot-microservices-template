package com.example.test.web.client.reactive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.web.reactive.function.client.WebClient;

/** Proxy for {@code WebClient.Builder} objects. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class BuilderProxy implements InvocationHandler {
  private final WebClient.Builder builder;
  private final Consumer<WebClient.Builder> customizer;

  public static WebClient.Builder create(
      WebClient.Builder builder, Consumer<WebClient.Builder> customizer) {

    var type = builder.getClass();
    return (WebClient.Builder)
        Proxy.newProxyInstance(
            type.getClassLoader(), type.getInterfaces(), new BuilderProxy(builder, customizer));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object @Nullable [] args) throws Throwable {
    if ("build".equals(method.getName()) && args == null) {
      return ClientProxy.create(builder.clone().apply(customizer).build(), customizer);
    }

    var result = invokeMethod(method, args);
    if (result == builder) {
      return proxy;
    } else if (result instanceof WebClient.Builder clone && "clone".equals(method.getName())) {
      return create(clone, customizer);
    }

    return result;
  }

  private Object invokeMethod(Method method, Object @Nullable [] args) throws Throwable {
    try {
      return method.invoke(builder, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
