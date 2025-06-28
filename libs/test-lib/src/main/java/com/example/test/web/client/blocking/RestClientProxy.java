package com.example.test.web.client.blocking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.web.client.RestClient;

/** Proxy for {@link RestClient} objects. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class RestClientProxy implements InvocationHandler {
  private final RestClient client;
  private final Consumer<RestClient.Builder> customizer;

  public static RestClient create(RestClient client, Consumer<RestClient.Builder> customizer) {

    var type = client.getClass();
    return (RestClient)
        Proxy.newProxyInstance(
            type.getClassLoader(), type.getInterfaces(), new RestClientProxy(client, customizer));
  }

  @Override
  public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
    try {
      var result = invokeMethod(method, args);
      if (result instanceof RestClient.Builder builder) {
        return RestClientBuilderProxy.create(builder, customizer);
      }

      return result;
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  private Object invokeMethod(Method method, @Nullable Object[] args) throws Throwable {
    try {
      return method.invoke(client, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
