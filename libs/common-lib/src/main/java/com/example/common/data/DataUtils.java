package com.example.common.data;

import com.example.common.error.exception.BadRequestException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Sort;

/** Data utilities. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataUtils {

  /**
   * Parses a {@link Sort} object from the given string.
   *
   * @param sort sort string in the format {@code property[:direction]} where {@code direction} is
   *     either {@code asc} or {@code desc}, can be {@code null}.
   * @param allowedFields set of allowed field names
   * @return parsed {@code Sort}
   * @throws BadRequestException if the sort string is invalid
   */
  public static Sort parseSort(@Nullable String sort, Set<String> allowedFields)
      throws BadRequestException {

    var order = parseSortOrder(sort, allowedFields);
    return order != null ? Sort.by(order) : Sort.unsorted();
  }

  /**
   * Parses a {@link Sort} object from the given list of strings.
   *
   * @param sortList list of sort strings, can be {@code null}. Sort string format is {@code
   *     property[:direction]} where {@code direction} is either {@code asc} or {@code desc}, can be
   *     {@code null}.
   * @param allowedFields set of allowed field names
   * @return parsed {@code Sort} object
   * @throws BadRequestException if any sort string is invalid
   */
  public static Sort parseSort(@Nullable List<String> sortList, Set<String> allowedFields)
      throws BadRequestException {
    if (sortList == null || sortList.isEmpty()) {
      return Sort.unsorted();
    }

    var orders =
        sortList.stream()
            .map(s -> parseSortOrder(s, allowedFields))
            .filter(Objects::nonNull)
            .toList();
    return Sort.by(orders);
  }

  private static Sort.@Nullable Order parseSortOrder(
      @Nullable String sort, Set<String> allowedFields) throws BadRequestException {

    if (sort == null || sort.isBlank()) {
      return null;
    }

    var parts = sort.split(":");
    var property = parts[0].strip();
    if (!allowedFields.contains(property)) {
      throw new BadRequestException(
          MessageFormat.format(
              "Invalid field \"{0}\", must be one of {1}", property, allowedFields));
    }

    if (parts.length == 1) {
      return Sort.Order.by(property);
    } else {
      var direction = parts[1].toLowerCase();
      if ("asc".equals(direction)) {
        return Sort.Order.asc(property);
      } else if ("desc".equals(direction)) {
        return Sort.Order.desc(property);
      } else {
        throw new BadRequestException(
            MessageFormat.format("Invalid direction \"{0}\", must \"asc\" or \"desc\"", parts[1]));
      }
    }
  }
}
