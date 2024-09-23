package com.example.common.uuid;

import java.util.Arrays;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** UUID utilities. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UuidUtils {
  /**
   * Creates a new random UUID.
   *
   * @param type required type of UUID
   * @return new UUID instance
   */
  public static UUID randomUUID(UuidType type) {
    var randomUUID = UUID.randomUUID();
    var msb =
        (randomUUID.getMostSignificantBits() & ~(0xFFFFL << 48)) | (((long) type.prefix) << 48);
    return new UUID(msb, randomUUID.getLeastSignificantBits());
  }

  /**
   * Returns the type of the given UUID.
   *
   * @param uuid UUID instance
   */
  public static UuidType typeOf(UUID uuid) {
    var prefix = (short) (uuid.getMostSignificantBits() >> 48);
    return Arrays.stream(UuidType.values())
        .filter(t -> t.prefix == prefix)
        .findFirst()
        .orElse(UuidType.UNKNOWN);
  }
}
