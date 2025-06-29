package com.example.test.data.h2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/** Proxy for {@link Statement} objects. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class StatementProxy implements InvocationHandler {
  private static final Set<String> UPDATE_METHODS = Set.of("executeUpdate", "executeBatch");
  private static final Pattern ALTER_TABLE_PATTERN =
      Pattern.compile(
          "^\\s*ALTER\\s+TABLE\\s+(`?\\w+`?)\\s+(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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

    if ("execute".equals(method.getName())) {
      return execute((String) args[0]);
    }

    return invokeMethod(method, args);
  }

  private void validateReadOnly(Method method) throws SQLException {
    var methodName = method.getName();
    if (UPDATE_METHODS.contains(methodName) && connection.isReadOnly()) {
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

  boolean execute(String sql) throws SQLException {
    var matcher = ALTER_TABLE_PATTERN.matcher(sql);
    if (matcher.matches()) {
      return executeAlterTable(matcher);
    }

    //noinspection SqlSourceToSinkFlow
    return statement.execute(sql);
  }

  @SuppressWarnings("SameReturnValue")
  private boolean executeAlterTable(Matcher matcher) throws SQLException {
    var tableName = matcher.group(1);
    var operations = splitAlterOperations(matcher.group(2).trim());

    for (var operation : operations) {
      //noinspection SqlSourceToSinkFlow
      statement.execute("ALTER TABLE " + tableName + " " + operation);
    }

    return false;
  }

  public List<String> splitAlterOperations(String input) {
    var result = new ArrayList<String>();
    var sb = new StringBuilder();
    boolean inBacktick = false;
    var inSingleQuote = false;
    var inDoubleQuote = false;
    int parenDepth = 0;
    var afterBackslash = false;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (inBacktick) {
        inBacktick = c != '`';
      } else if (inSingleQuote) {
        inSingleQuote = c != '\'' || afterBackslash;
      } else if (inDoubleQuote) {
        inDoubleQuote = c != '\"' || afterBackslash;
      } else if (c == '`') {
        inBacktick = true;
      } else if (c == '\'') {
        inSingleQuote = true;
      } else if (c == '\"') {
        inDoubleQuote = true;
      } else if (c == '(') {
        parenDepth++;
      } else if (c == ')') {
        parenDepth--;
      } else if (c == ',' && parenDepth == 0) {
        afterBackslash = false;
        result.add(sb.toString().trim());
        sb.setLength(0);
        continue;
      }

      afterBackslash = c == '\\' && !afterBackslash;
      sb.append(c);
    }

    if (!sb.isEmpty()) {
      result.add(sb.toString().trim());
    }

    return result;
  }
}
