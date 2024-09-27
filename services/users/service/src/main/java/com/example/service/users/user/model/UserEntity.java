package com.example.service.users.user.model;

import com.example.common.data.jpa.JpaUtils;
import com.example.common.data.jpa.jdbc.NullableTimestampJdbcType;
import com.example.common.uuid.UuidType;
import com.example.common.uuid.UuidUtils;
import com.example.service.users.role.model.RoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.springframework.lang.Nullable;

@Getter
@Setter
@ToString
@Entity
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "binary(16)")
  private UUID uuid;

  private String email;

  @Column(columnDefinition = "tinyint(1)")
  private boolean enabled;

  private String firstName;
  private String lastName;
  private String password;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Instant createdAt;

  @JdbcType(NullableTimestampJdbcType.class)
  private Instant deletedAt;

  @ManyToMany
  @JoinTable(
      name = "user_role",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @ToString.Exclude
  private Set<RoleEntity> roles = new HashSet<>();

  public static UserEntity createForInsert() {
    var user = new UserEntity();
    user.setUuid(UuidUtils.randomUUID(UuidType.USER));
    user.setEnabled(true);
    user.setDeletedAt(JpaUtils.SOFT_NULL_INSTANT);

    return user;
  }

  public boolean isDeleted() {
    return !JpaUtils.SOFT_NULL_INSTANT.equals(deletedAt);
  }

  @Nullable
  public Instant getDeletedAt() {
    return JpaUtils.SOFT_NULL_INSTANT.equals(deletedAt) ? null : deletedAt;
  }

  public void setDeletedAt(@Nullable Instant deletedAt) {
    this.deletedAt = deletedAt == null ? JpaUtils.SOFT_NULL_INSTANT : deletedAt;
  }

  @Override
  @SuppressWarnings({"EqualsDoesntCheckParameterClass", "EqualsWhichDoesntCheckParameterClass"})
  public boolean equals(Object o) {
    return JpaUtils.entityEquals(this, o, UserEntity::getId);
  }

  @Override
  public int hashCode() {
    return JpaUtils.entityHashCode(this, UserEntity::getId);
  }
}
