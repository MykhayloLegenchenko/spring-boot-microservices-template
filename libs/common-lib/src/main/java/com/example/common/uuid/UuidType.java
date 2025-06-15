package com.example.common.uuid;

/** Represents the type of UUID. */
public enum UuidType {
  // users-service types
  USER(0x1001),

  UNKNOWN(0xFFFF);

  final int prefix;

  UuidType(int prefix) {
    assert prefix >= 0 && prefix <= 0xFFFF;

    this.prefix = prefix;
  }
}
