package com.example.test.data.h2;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for the H2 database driver.
 *
 * <p>Supports read-only connections.
 */
@Slf4j
public final class DriverWrapper extends ForwardingDriver {
  private static final String PREFIX = "wrapper:h2:";
  private static final String H2_PREFIX = "jdbc:h2:";

  private final Driver delegate;

  static {
    load();
  }

  public DriverWrapper() throws SQLException {
    this.delegate = Objects.requireNonNull(DriverManager.getDriver(H2_PREFIX));
  }

  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    var connection = super.connect(convertUrl(url), info);
    return connection != null ? ConnectionProxy.create(connection) : null;
  }

  @Override
  public boolean acceptsURL(String url) throws SQLException {
    return url != null && url.startsWith(PREFIX) && super.acceptsURL(convertUrl(url));
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
    return delegate().getPropertyInfo(convertUrl(url), info);
  }

  private static void load() {
    try {
      DriverManager.registerDriver(new DriverWrapper());
    } catch (Exception ex) {
      log.error("Cannot register driver", ex);
    }
  }

  private static String convertUrl(String url) {
    if (!url.startsWith(PREFIX)) {
      throw new IllegalArgumentException("Invalid url: " + url);
    }

    return H2_PREFIX + url.substring(PREFIX.length());
  }

  @Override
  protected Driver delegate() {
    return delegate;
  }
}
