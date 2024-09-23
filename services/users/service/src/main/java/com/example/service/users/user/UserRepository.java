package com.example.service.users.user;

import com.example.common.data.jpa.JpaUtils;
import com.example.common.data.jpa.repository.support.ExtendedJpaRepository;
import com.example.service.users.user.model.UserEntity;
import com.example.service.users.user.model.UserEntity_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends ExtendedJpaRepository<UserEntity, Long> {

  interface Spec {
    static Specification<UserEntity> byUuid(UUID uuid) {
      return (root, query, builder) -> builder.equal(root.get(UserEntity_.uuid), uuid);
    }

    static Specification<UserEntity> byEmail(String email) {
      return ((root, query, builder) -> builder.equal(root.get(UserEntity_.email), email));
    }

    static Specification<UserEntity> byDeletedAt(@Nullable Instant deletedAt) {
      return (root, query, builder) ->
          builder.equal(
              root.get(UserEntity_.deletedAt),
              deletedAt != null ? deletedAt : JpaUtils.SOFT_NULL_INSTANT);
    }

    static Specification<UserEntity> withRoles(Specification<UserEntity> spec) {
      return (root, query, builder) -> {
        root.fetch(UserEntity_.roles, JoinType.LEFT);
        return spec.toPredicate(root, query, builder);
      };
    }
  }

  @Transactional
  default long softDelete(EntityManager em, Specification<UserEntity> spec) {
    var cb = em.getCriteriaBuilder();
    var update = cb.createCriteriaUpdate(UserEntity.class);
    var user = update.from(UserEntity.class);

    update
        .where(spec.toPredicate(user, null, cb))
        .set(user.get(UserEntity_.deletedAt), Instant.now());

    return em.createQuery(update).executeUpdate();
  }

  @Modifying
  @Query(
      nativeQuery = true,
      value = "DELETE FROM `user_role` WHERE `user_id`=:userId AND `role_id` IN(:roleIds)")
  void deleteRolesByIds(@Param("userId") Long userId, @Param("roleIds") Collection<Long> roleIds);
}
