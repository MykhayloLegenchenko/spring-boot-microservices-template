package com.example.common.data.jpa;

import com.example.common.error.exception.ConflictException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/** JPA utilities. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JpaUtils {
  /**
   * The "null" instant value used in the "soft delete" algorithm for tables with a unique
   * constraints.
   */
  public static final Instant SOFT_NULL_INSTANT =
      Instant.from(DateTimeFormatter.ISO_INSTANT.parse("1000-01-01T00:00:00Z"));

  /** SQL datetime string for {@link #SOFT_NULL_INSTANT}. */
  public static final String SOFT_NULL_DATETIME_STR = "1000-01-01 00:00:00";

  private static final boolean H2_PRESENT =
      ClassUtils.isPresent("org.h2.Driver", JpaUtils.class.getClassLoader());

  /**
   * Implementation of the {@code equals} method for Hibernate JPA entities.
   *
   * <p>Inspired by this <a
   * href="https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/">article</a>
   *
   * @param a JPA entity
   * @param b entity to be compared with {@code a}
   * @param idExtractor id extractor function
   * @return {@code true} if the entities have same effective classes and equal identifiers,
   *     otherwise {@code false}
   * @param <T> entity type
   * @see java.util.Objects#equals(Object, Object)
   */
  public static <T> boolean entityEquals(
      @Nullable T a, @Nullable Object b, Function<? super T, ?> idExtractor) {

    if (a == b) return true;
    if (a == null || b == null) return false;

    if (getEffectiveClass(a) != getEffectiveClass(b)) return false;

    var idA = idExtractor.apply(a);
    if (idA == null) return false;

    // This cast is correct because the effective classes of a and b are same.
    @SuppressWarnings("unchecked")
    var idB = idExtractor.apply((T) b);

    return idA.equals(idB);
  }

  /**
   * Implementation of the {@code hashCode} method for Hibernate JPA entities.
   *
   * <p>Inspired by this <a
   * href="https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/">article</a>
   *
   * <p>Returns the combined hash code of the entity ID and effective class. Seems like a better
   * solution than the one in the article which only returns the hash code of the effective class.
   *
   * @param entity JPA entity
   * @param idExtractor id extractor function
   * @return hash code value for the entity
   * @param <T> entity type
   * @see Object#hashCode()
   */
  public static <T> int entityHashCode(T entity, Function<? super T, ?> idExtractor) {
    return entityHashCode(idExtractor.apply(entity), getEffectiveClass(entity).hashCode());
  }

  /**
   * Throws a {@link ConflictException} with the reason supplied by the {@code reasonSupplier} if
   * the original exception {@code ex} is caused by a violation of the database constraint named
   * {@code constraintName}. Otherwise, throws the original exception {@code ex}.
   *
   * <p>Provides clear error messages when database constraints are violated.
   *
   * @param ex original exception
   * @param constraintName constraint name
   * @param reasonSupplier exception reason supplier
   */
  public static void processConstraintViolation(
      RuntimeException ex, String constraintName, @Nullable Supplier<String> reasonSupplier) {
    if (checkConstraintName(ex, constraintName)) {
      throw new ConflictException(reasonSupplier != null ? reasonSupplier.get() : null, ex);
    } else {
      throw ex;
    }
  }

  /**
   * Sets the first result offset for the criteria query.
   *
   * <p>Allows to set {@code long} offsets for the query result while {@link
   * Query#setFirstResult(int)} only supports {@code int} offsets.
   *
   * @param cq criteria query
   * @param offset first result offset
   */
  public static void offset(CriteriaQuery<?> cq, long offset) {
    ((JpaCriteriaQuery<?>) cq).offset(offset);
  }

  /**
   * Sets the first result offset and the maximum number of results for the criteria query.
   *
   * <p>Allows to set {@code long} offsets for the query result while {@link
   * Query#setFirstResult(int)} only supports {@code int} offsets.
   *
   * @param cq criteria query
   * @param offset first result offset
   * @param limit maximum number of results
   */
  public static void limit(CriteriaQuery<?> cq, long offset, int limit) {
    offset(cq, offset);
    ((JpaCriteriaQuery<?>) cq).fetch(limit);
  }

  private static Class<?> getEffectiveClass(Object o) {
    return o instanceof HibernateProxy hp
        ? hp.getHibernateLazyInitializer().getPersistentClass()
        : o.getClass();
  }

  private static boolean checkConstraintName(Throwable t, String expectedName) {
    while (t != null) {
      if (t instanceof ConstraintViolationException cve) {
        return checkConstraint(cve.getConstraintName(), expectedName);
      }

      t = t.getCause();
    }

    return false;
  }

  private static boolean checkConstraint(@Nullable String name, String expectedName) {
    if (!H2_PRESENT) {
      return expectedName.equals(name);
    }

    if (name == null) {
      return false;
    }

    var i = name.indexOf('.');
    if (i != -1) {
      name = name.substring(i + 1);
    }

    i = expectedName.indexOf('.');
    if (i != -1) {
      expectedName = expectedName.substring(i + 1);
    }

    return name.toLowerCase().contains(expectedName.toLowerCase());
  }

  private static int entityHashCode(@Nullable Object a, @Nullable Object b) {
    int result = 1;
    result = result * 59 + (a == null ? 43 : a.hashCode());
    result = result * 59 + (b == null ? 43 : b.hashCode());

    return result;
  }
}
