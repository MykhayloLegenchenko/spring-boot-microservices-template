package com.example.test.data.h2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/** Proxy for {@code Statement} objects. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class StatementProxy implements InvocationHandler {
  private static final Set<String> UPDATE_METHODS = Set.of("executeUpdate", "executeBatch");

  private final Statement statement;
  private final Connection connection;

  public static Statement create(Statement statement, Connection connection) {
    return (Statement)
        Proxy.newProxyInstance(
            statement.getClass().getClassLoader(),
            statement.getClass().getInterfaces(),
            new StatementProxy(statement, connection));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    validateReadOnly(method);
    return invokeMethod(method, args);
  }

  private void validateReadOnly(Method method) throws SQLException {
    if (UPDATE_METHODS.contains(method.getName()) && connection.isReadOnly()) {
      throw new SQLException(
          "Connection is read-only. Queries leading to data modification are not allowed", "S1009");
    }
  }

  private Object invokeMethod(Method method, Object[] args) throws Throwable {
    try {
      return method.invoke(statement, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
