package com.example.service.users.user;

import com.example.client.users.user.dto.FindUsersFilter;
import com.example.common.data.jpa.JpaUtils;
import com.example.common.data.jpa.repository.support.ExtendedJpaRepository;
import com.example.service.users.user.model.UserEntity;
import com.example.service.users.user.model.UserEntity_;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;

public interface UserRepository extends ExtendedJpaRepository<UserEntity, Long> {

  interface Spec {
    static Specification<UserEntity> byUuid(UUID uuid) {
      return (root, query, cb) -> cb.equal(root.get(UserEntity_.uuid), uuid);
    }

    static Specification<UserEntity> byEmail(String email) {
      return (root, query, cb) -> cb.equal(root.get(UserEntity_.email), email);
    }

    static Specification<UserEntity> byDeletedAt(@Nullable Instant deletedAt) {
      return (root, query, cb) ->
          cb.equal(
              root.get(UserEntity_.deletedAt),
              deletedAt != null ? deletedAt : JpaUtils.SOFT_NULL_INSTANT);
    }

    static Specification<UserEntity> byUsersFilter(FindUsersFilter filter) {
      return (root, query, cb) -> {
        var predicates = new ArrayList<Predicate>();
        if (filter.search() != null) {
          var search = '%' + EscapeCharacter.DEFAULT.escape(filter.search()) + '%';
          predicates.add(
              cb.or(
                  cb.like(root.get(UserEntity_.email), search),
                  cb.like(root.get(UserEntity_.firstName), search),
                  cb.like(root.get(UserEntity_.lastName), search)));
        }

        if (filter.enabled() != null) {
          predicates.add(cb.equal(root.get(UserEntity_.enabled), filter.enabled()));
        }

        if (filter.deleted() != null) {
          var deletedAt = root.get(UserEntity_.deletedAt);
          predicates.add(
              Boolean.TRUE.equals(filter.deleted())
                  ? cb.notEqual(deletedAt, JpaUtils.SOFT_NULL_INSTANT)
                  : cb.equal(deletedAt, JpaUtils.SOFT_NULL_INSTANT));
        }

        return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
      };
    }

    static Specification<UserEntity> withRoles(Specification<UserEntity> spec) {
      return (root, query, cb) -> {
        root.fetch(UserEntity_.roles, JoinType.LEFT);
        return spec.toPredicate(root, query, cb);
      };
    }
  }
}
