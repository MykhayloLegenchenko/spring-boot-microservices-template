package com.example.service.users.user;

import static com.example.service.users.user.UserRepository.Spec.*;
import static java.util.function.Predicate.not;

import com.example.client.users.user.dto.CountUsersRequest;
import com.example.client.users.user.dto.FindUsersFilter;
import com.example.client.users.user.dto.FindUsersRequest;
import com.example.client.users.user.dto.RegisterUserRequest;
import com.example.client.users.user.dto.UpdateUserRequest;
import com.example.client.users.user.dto.UserData;
import com.example.client.users.user.dto.UserDto;
import com.example.client.users.user.dto.UserDtoEx;
import com.example.common.data.DataUtils;
import com.example.common.data.OffsetPageRequest;
import com.example.common.data.jpa.JpaUtils;
import com.example.common.dto.CountResult;
import com.example.common.error.exception.NotFoundException;
import com.example.service.users.role.RoleRepository;
import com.example.service.users.role.model.RoleEntity;
import com.example.service.users.user.model.UserEntity;
import com.example.service.users.user.model.UserEntity_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
  private static final String USER_NOT_FOUND = "User is not found.";
  private static final Set<String> SORT_FIELDS =
      UserEntity_.class_.getAttributes().stream()
          .map(Attribute::getName)
          .collect(Collectors.toSet());

  private final EntityManager em;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Getter
  @RequiredArgsConstructor
  public enum Access {
    USER(false),
    ADMIN(true),
    SUPER(true);

    private final boolean admin;

    public boolean isSuper() {
      return this == SUPER;
    }
  }

  @Transactional
  public UserDto registerUser(RegisterUserRequest request) {
    var user = UserEntity.createForInsert();
    userMapper.update(user, request);

    user.setPassword(passwordEncoder.encode(request.password()));
    save(user);

    return userMapper.toUserDto(user);
  }

  public UserData getUser(UUID uuid, Access access) {
    return toUserDto(findUser(byUuid(uuid), access, false), access);
  }

  public List<UserDtoEx> findUsers(FindUsersRequest request) {
    var cb = em.getCriteriaBuilder();
    var select = cb.createQuery(UserEntity.class);
    var user = select.from(UserEntity.class);
    select.select(user);

    var predicates = getSearchPredicates(request, cb, user);
    if (predicates.length > 0) {
      select.where(cb.and(predicates));
    }

    var pageRequest =
        OffsetPageRequest.of(
            request.offset(), request.limit(), DataUtils.parseSort(request.sort(), SORT_FIELDS));
    var orderBy =
        pageRequest
            .getSort()
            .get()
            .map(
                s -> {
                  var path = user.get(s.getProperty());
                  return s.getDirection().isAscending() ? cb.asc(path) : cb.desc(path);
                })
            .toArray(Order[]::new);
    if (orderBy.length > 0) {
      select.orderBy(orderBy);
    }

    JpaUtils.limit(select, pageRequest.getOffset(), pageRequest.getPageSize());

    return em.createQuery(select).getResultList().stream().map(userMapper::toUserDtoEx).toList();
  }

  public CountResult countUsers(CountUsersRequest request) {
    var cb = em.getCriteriaBuilder();
    var cq = cb.createQuery(Long.class);
    var users = cq.from(UserEntity.class);
    cq.select(cb.count(users));

    var predicates = getSearchPredicates(request, cb, users);
    if (predicates.length > 0) {
      cq.where(cb.and(predicates));
    }

    return new CountResult(em.createQuery(cq).getSingleResult());
  }

  @Transactional
  public UserData updateUser(UUID uuid, UpdateUserRequest request, Access access) {
    var user = findUser(byUuid(uuid), access, false);

    userMapper.update(user, request);
    save(user);

    return toUserDto(user, access);
  }

  @Transactional
  public void setUserEnabled(UUID uuid, boolean enabled) {
    var user = findUser(byUuid(uuid), Access.ADMIN, false);

    user.setEnabled(enabled);
  }

  @Transactional
  public void deleteUser(UUID uuid) {
    if (userRepository.softDelete(em, byUuid(uuid).and(byDeletedAt(null))) == 0) {
      throw new NotFoundException(USER_NOT_FOUND);
    }
  }

  public Set<String> getRoles(UUID uuid, Access access) {
    return findUser(withRoles(byUuid(uuid)), access, true).getRoles().stream()
        .map(RoleEntity::getName)
        .collect(Collectors.toSet());
  }

  @Transactional
  public void setRoles(UUID uuid, Set<String> roles, Access access) {
    var user = findUser(withRoles(byUuid(uuid)), access, true);

    roles = roles.stream().map(String::toUpperCase).collect(Collectors.toSet());

    var existingRolesMap =
        user.getRoles().stream()
            .collect(Collectors.toMap(RoleEntity::getName, Function.identity()));

    // Only superuser can add/remove protected roles
    if (!access.isSuper()) {
      checkProtectedRoles(existingRolesMap, roles);
    }

    addNewRoles(user, existingRolesMap, roles);
    removeOldRoles(user, existingRolesMap, roles);
  }

  private void checkProtectedRoles(Map<String, RoleEntity> existingRolesMap, Set<String> roles) {
    for (var name : RoleRepository.PROTECTED_NAMES) {
      if (roles.contains(name) && !existingRolesMap.containsKey(name)) {
        throw new AccessDeniedException("Cannot add protected role \"" + name + '"');
      } else if (existingRolesMap.containsKey(name) && !roles.contains(name)) {
        throw new AccessDeniedException("Cannot remove protected role \"" + name + '"');
      }
    }
  }

  private void addNewRoles(
      UserEntity user, Map<String, RoleEntity> existingRolesMap, Set<String> roles) {

    var newRoles =
        roles.stream().filter(not(existingRolesMap::containsKey)).collect(Collectors.toSet());
    if (newRoles.isEmpty()) {
      return;
    }

    var newRoleEntities = roleRepository.findAll(RoleRepository.Spec.byNames(newRoles));
    if (newRoles.size() != newRoleEntities.size()) {
      var notFoundRoleNames = new HashSet<>(newRoles);
      notFoundRoleNames.removeAll(
          newRoleEntities.stream().map(RoleEntity::getName).collect(Collectors.toSet()));

      throw new NotFoundException("Cannot find roles " + notFoundRoleNames);
    }

    var session = em.unwrap(Session.class);
    session.doWork(con -> insertNewRoles(con, user, newRoleEntities));
  }

  private void insertNewRoles(Connection con, UserEntity user, List<RoleEntity> roles)
      throws SQLException {
    try (var ps =
        con.prepareStatement("INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (?, ?)")) {

      ps.setLong(1, user.getId());
      for (var role : roles) {
        ps.setLong(2, role.getId());
        ps.addBatch();
      }

      ps.executeBatch();
    }
  }

  private void removeOldRoles(
      UserEntity user, Map<String, RoleEntity> existingRolesMap, Set<String> roles) {
    var deleteRoleIds =
        existingRolesMap.values().stream()
            .filter(r -> !roles.contains(r.getName()))
            .map(RoleEntity::getId)
            .toList();
    if (!deleteRoleIds.isEmpty()) {
      userRepository.deleteRolesByIds(user.getId(), deleteRoleIds);
    }
  }

  private static Predicate[] getSearchPredicates(
      FindUsersFilter request, CriteriaBuilder cb, Root<UserEntity> users) {
    var predicates = new ArrayList<Predicate>();
    if (request.search() != null) {
      var search = '%' + EscapeCharacter.DEFAULT.escape(request.search()) + '%';
      predicates.add(
          cb.or(
              cb.like(users.get(UserEntity_.email), search),
              cb.like(users.get(UserEntity_.firstName), search),
              cb.like(users.get(UserEntity_.lastName), search)));
    }

    if (request.enabled() != null) {
      predicates.add(cb.equal(users.get(UserEntity_.enabled), request.enabled()));
    }

    if (request.deleted() != null) {
      var deletedAt = users.get(UserEntity_.deletedAt);
      predicates.add(
          Boolean.TRUE.equals(request.deleted())
              ? cb.notEqual(deletedAt, JpaUtils.SOFT_NULL_INSTANT)
              : cb.equal(deletedAt, JpaUtils.SOFT_NULL_INSTANT));
    }

    return predicates.toArray(Predicate[]::new);
  }

  private UserEntity findUser(Specification<UserEntity> spec, Access access, boolean fetch) {
    var user = fetch ? userRepository.fetchOne(spec) : userRepository.findOne(spec);
    return user.filter(u -> access.isAdmin() || (u.isEnabled() && !u.isDeleted()))
        .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
  }

  private void save(UserEntity user) {
    try {
      if (user.getId() == null) {
        userRepository.persistAndFlush(user);
      } else {
        userRepository.flush();
      }
    } catch (DataIntegrityViolationException ex) {
      JpaUtils.processConstraintViolation(
          ex,
          "user.user_email_uk",
          () -> MessageFormat.format("User with email \"{0}\" already exists.", user.getEmail()));
    }
  }

  private UserData toUserDto(UserEntity user, Access access) {
    return access.isAdmin() ? userMapper.toUserDtoEx(user) : userMapper.toUserDto(user);
  }
}
