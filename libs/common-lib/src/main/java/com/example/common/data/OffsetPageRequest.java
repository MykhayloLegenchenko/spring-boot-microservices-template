package com.example.common.data;

import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** {@code Pageable} based on page offset. */
@EqualsAndHashCode
public class OffsetPageRequest implements Pageable {
  private final long offset;
  private final int limit;
  private final Sort sort;

  private OffsetPageRequest(long offset, int limit, Sort sort) {
    this.offset = offset;
    this.limit = limit;
    this.sort = sort;
  }

  /**
   * Creates a {@code Pageable} object.
   *
   * @param offset page offset, can be {@code null}
   * @param limit page limit, can be {@code null}
   * @param sort sort order, can be {@code null}
   */
  public static Pageable of(@Nullable Long offset, @Nullable Integer limit, @Nullable Sort sort) {
    return new OffsetPageRequest(
        offset != null ? offset : 0,
        limit != null ? limit : 20,
        sort != null ? sort : Sort.unsorted());
  }

  @Override
  public int getPageNumber() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public Pageable next() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pageable previousOrFirst() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pageable first() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Pageable withPage(int pageNumber) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasPrevious() {
    return false;
  }
}
