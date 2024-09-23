package com.example.test.web.client.blocking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClient;

/** Proxy for {@code RestClient.Builder} objects. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class BuilderProxy implements InvocationHandler {
  private final RestClient.Builder builder;
  private final Consumer<RestClient.Builder> customizer;

  public static RestClient.Builder create(
      RestClient.Builder builder, Consumer<RestClient.Builder> customizer) {

    var type = builder.getClass();
    return (RestClient.Builder)
        Proxy.newProxyInstance(
            type.getClassLoader(), type.getInterfaces(), new BuilderProxy(builder, customizer));
  }

  @Override
  public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
    if ("build".equals(method.getName()) && args == null) {
      return ClientProxy.create(builder.clone().apply(customizer).build(), customizer);
    }

    var result = invokeMethod(method, args);
    if (result == builder) {
      return proxy;
    } else if (result instanceof RestClient.Builder clone && "clone".equals(method.getName())) {
      return create(clone, customizer);
    }

    return result;
  }

  private Object invokeMethod(Method method, @Nullable Object[] args) throws Throwable {
    try {
      return method.invoke(builder, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
