package com.example.service.users.role.model;

import com.example.common.data.jpa.JpaUtils;
import com.example.service.users.user.model.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class RoleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToMany(mappedBy = "roles")
  @ToString.Exclude
  private Set<UserEntity> users = new HashSet<>();

  @Override
  @SuppressWarnings({"EqualsDoesntCheckParameterClass", "EqualsWhichDoesntCheckParameterClass"})
  public boolean equals(Object o) {
    return JpaUtils.entityEquals(this, o, RoleEntity::getId);
  }

  @Override
  public int hashCode() {
    return JpaUtils.entityHashCode(this, RoleEntity::getId);
  }
}
