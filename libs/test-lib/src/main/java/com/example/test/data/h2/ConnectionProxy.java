package com.example.test.data.h2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/** Proxy for {@code Connection} objects. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ConnectionProxy implements InvocationHandler {
  private final Connection connection;
  private boolean readOnly;

  public static Connection create(Connection connection) {
    var type = connection.getClass();

    return (Connection)
        Proxy.newProxyInstance(
            type.getClassLoader(), type.getInterfaces(), new ConnectionProxy(connection));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    switch (method.getName()) {
      case "setReadOnly":
        setReadOnly((Boolean) args[0]);
        return null;

      case "isReadOnly":
        return isReadOnly();

      default:
        var result = invokeMethod(method, args);
        if (result instanceof Statement statement) {
          return StatementProxy.create(statement, (Connection) proxy);
        }

        return result;
    }
  }

  private Object invokeMethod(Method method, Object[] args) throws Throwable {
    try {
      return method.invoke(connection, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  private void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  private boolean isReadOnly() throws SQLException {
    return readOnly || connection.isReadOnly();
  }
}
