package com.example.common.data.jpa.jdbc;

import com.example.common.data.jpa.JpaUtils;
import java.io.Serial;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;
import org.hibernate.type.descriptor.jdbc.TimestampUtcAsJdbcTimestampJdbcType;

/**
 * Custom JDBC type for instants.
 *
 * <p>Translates {@link JpaUtils#SOFT_NULL_INSTANT} instants to {@link
 * JpaUtils#SOFT_NULL_DATETIME_STR} SQL datetime.
 *
 * <p>Supports MySQL and H2.
 */
public class NullableTimestampJdbcType extends TimestampUtcAsJdbcTimestampJdbcType {
  public static final NullableTimestampJdbcType INSTANCE = new NullableTimestampJdbcType();
  private static final Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

  private NullableTimestampJdbcType() {}

  @Override
  public <X> ValueBinder<X> getBinder(final JavaType<X> javaType) {
    return new BasicBinder<>(javaType, this) {
      @Serial private static final long serialVersionUID = 3012767409508013784L;

      @Override
      protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
          throws SQLException {
        var instant = javaType.unwrap(value, Instant.class, options);
        if (JpaUtils.SOFT_NULL_INSTANT.equals(instant)) {
          st.setString(index, JpaUtils.SOFT_NULL_DATETIME_STR);
        } else {
          st.setTimestamp(index, Timestamp.from(instant), UTC_CALENDAR);
        }
      }

      @Override
      protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
          throws SQLException {
        var instant = javaType.unwrap(value, Instant.class, options);
        if (JpaUtils.SOFT_NULL_INSTANT.equals(instant)) {
          st.setString(name, JpaUtils.SOFT_NULL_DATETIME_STR);
        } else {
          st.setTimestamp(name, Timestamp.from(instant), UTC_CALENDAR);
        }
      }
    };
  }

  @Override
  public <X> ValueExtractor<X> getExtractor(final JavaType<X> javaType) {
    return new BasicExtractor<>(javaType, this) {
      @Serial private static final long serialVersionUID = -3385175409127219144L;

      private interface TimestampSupplier {
        Timestamp get() throws SQLException;
      }

      @Override
      protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options)
          throws SQLException {
        return doExtract(
            rs.getString(paramIndex), () -> rs.getTimestamp(paramIndex, UTC_CALENDAR), options);
      }

      @Override
      protected X doExtract(CallableStatement statement, int index, WrapperOptions options)
          throws SQLException {
        return doExtract(
            statement.getString(index), () -> statement.getTimestamp(index, UTC_CALENDAR), options);
      }

      @Override
      protected X doExtract(CallableStatement statement, String name, WrapperOptions options)
          throws SQLException {

        return doExtract(
            statement.getString(name), () -> statement.getTimestamp(name, UTC_CALENDAR), options);
      }

      private X doExtract(
          String strVal, TimestampSupplier timestampSupplier, WrapperOptions options)
          throws SQLException {
        if (JpaUtils.SOFT_NULL_DATETIME_STR.equals(strVal)) {
          return javaType.wrap(JpaUtils.SOFT_NULL_INSTANT, options);
        }

        var timestamp = timestampSupplier.get();
        return javaType.wrap(timestamp == null ? null : timestamp.toInstant(), options);
      }
    };
  }
}
