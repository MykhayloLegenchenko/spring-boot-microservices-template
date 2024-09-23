package com.example.test.web.client.reactive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClient;

/** Proxy for {@code WebClient} objects. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ClientProxy implements InvocationHandler {
  private final WebClient client;
  private final Consumer<WebClient.Builder> customizer;

  public static WebClient create(WebClient client, Consumer<WebClient.Builder> customizer) {

    var type = client.getClass();
    return (WebClient)
        Proxy.newProxyInstance(
            type.getClassLoader(), type.getInterfaces(), new ClientProxy(client, customizer));
  }

  @Override
  public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
    var result = invokeMethod(method, args);
    if (result instanceof WebClient.Builder builder) {
      return BuilderProxy.create(builder, customizer);
    }

    return result;
  }

  private Object invokeMethod(Method method, @Nullable Object[] args) throws Throwable {
    try {
      return method.invoke(client, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
