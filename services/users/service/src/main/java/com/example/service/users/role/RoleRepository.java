package com.example.service.users.role;

import com.example.common.data.jpa.repository.support.ExtendedJpaRepository;
import com.example.service.users.role.model.RoleEntity;
import com.example.service.users.role.model.RoleEntity_;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public interface RoleRepository extends ExtendedJpaRepository<RoleEntity, Long> {

  Set<String> RESERVED_NAMES = Set.of("USER", "REFRESH");
  Set<String> PROTECTED_NAMES = Set.of("ADMIN", "SUPER");

  interface Spec {
    static Specification<RoleEntity> byName(String name) {
      return (root, query, builder) -> builder.equal(root.get(RoleEntity_.name), name);
    }

    static Specification<RoleEntity> byNames(Collection<String> names) {
      return (root, query, builder) -> root.get(RoleEntity_.name).in(names);
    }
  }

  List<RoleEntity> findAll(Sort sort);
}
