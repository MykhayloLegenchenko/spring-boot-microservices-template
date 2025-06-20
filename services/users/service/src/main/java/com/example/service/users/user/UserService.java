package com.example.service.users.user;

import static com.example.client.users.user.dto.UserEvent.TOPIC;
import static com.example.client.users.user.dto.UserEvent.Type.*;
import static com.example.service.users.user.UserRepository.Spec.*;

import com.example.client.users.user.dto.*;
import com.example.common.data.DataUtils;
import com.example.common.data.OffsetPageRequest;
import com.example.common.data.jpa.JpaUtils;
import com.example.common.dto.CountResult;
import com.example.common.error.exception.BadRequestException;
import com.example.common.error.exception.NotFoundException;
import com.example.service.users.role.RoleRepository;
import com.example.service.users.role.model.RoleEntity;
import com.example.service.users.user.model.UserEntity;
import com.example.service.users.user.model.UserEntity_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@SuppressWarnings("FutureReturnValueIgnored")
public class UserService {
  private static final String USER_NOT_FOUND = "User is not found.";
  private static final Set<String> SORT_FIELDS =
      UserEntity_.class_.getAttributes().stream()
          .map(Attribute::getName)
          .collect(Collectors.toSet());

  private final PasswordEncoder passwordEncoder;
  private final EntityManager em;
  private final KafkaTemplate<String, UserEvent> userKafkaTemplate;
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

    public boolean notSuper() {
      return this != SUPER;
    }
  }

  @Bean
  public NewTopic userTopic() {
    return TopicBuilder.name(TOPIC).build();
  }

  @Transactional
  public UserDto registerUser(RegisterUserRequest request) {
    var user = UserEntity.createForInsert();

    userMapper.update(user, request);
    user.setPassword(passwordEncoder.encode(request.password()));
    save(user);

    userKafkaTemplate.send(
        TOPIC,
        user.getUuid().toString(),
        new UserEvent(UserEvent.Type.REGISTER, userMapper.toUserDtoEx(user)));

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

    var predicate = byUsersFilter(request).toPredicate(user, select, cb);
    if (predicate != null) {
      select.where(predicate);
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
    var count = userRepository.count(byUsersFilter(request));
    return new CountResult(count);
  }

  @Transactional
  public UserData updateUser(UUID uuid, UpdateUserRequest request, Access access) {
    var user = findUser(byUuid(uuid), access, false);
    userMapper.update(user, request);
    save(user);

    userKafkaTemplate.send(
        TOPIC, user.getUuid().toString(), new UserEvent(UPDATE, userMapper.toUserDtoEx(user)));

    return toUserDto(user, access);
  }

  @Transactional
  public void setUserEnabled(UUID uuid, boolean enabled) {
    var user = findUser(byUuid(uuid), Access.ADMIN, false);
    user.setEnabled(enabled);
    roleRepository.flush();

    userKafkaTemplate.send(
        TOPIC,
        user.getUuid().toString(),
        new UserEvent(enabled ? ENABLE : DISABLE, userMapper.toUserDtoEx(user)));
  }

  @Transactional
  public void deleteUser(UUID uuid) {
    var user = findUser(byUuid(uuid), Access.ADMIN, false);
    user.setDeletedAt(Instant.now());
    userRepository.flush();

    userKafkaTemplate.send(
        TOPIC, user.getUuid().toString(), new UserEvent(DELETE, userMapper.toUserDtoEx(user)));
  }

  public Set<String> getRoles(UUID uuid, Access access) {
    return findUser(withRoles(byUuid(uuid)), access, true).getRoles().stream()
        .map(RoleEntity::getName)
        .collect(Collectors.toSet());
  }

  @Transactional
  public void setRoles(UUID uuid, Set<String> roles, Access access) {
    var user = findUser(withRoles(byUuid(uuid)), access, true);
    var userRoles = user.getRoles();

    roles.forEach(
        role -> {
          if (RoleRepository.RESERVED_NAMES.contains(role)) {
            throw new BadRequestException(MessageFormat.format("Role name {0} is protected", role));
          }
        });

    // Remove old roles
    for (var it = userRoles.iterator(); it.hasNext(); ) {
      var name = it.next().getName();
      if (!roles.contains(name)) {
        if (access.notSuper() && RoleRepository.PROTECTED_NAMES.contains(name)) {
          throw new AccessDeniedException("Cannot remove protected role \"" + name + '"');
        }

        it.remove();
      }
    }

    // Add new roles
    var newRoles = new ArrayList<String>();
    for (String name : roles) {
      if (userRoles.stream().noneMatch(r -> name.equals(r.getName()))) {
        if (access.notSuper() && RoleRepository.PROTECTED_NAMES.contains(name)) {
          throw new AccessDeniedException("Cannot add protected role \"" + name + '"');
        }

        newRoles.add(name);
      }
    }

    userRoles.addAll(roleRepository.findAll(RoleRepository.Spec.byNames(newRoles)));

    userKafkaTemplate.send(
        TOPIC,
        user.getUuid().toString(),
        new UserEvent(
            SET_ROLES,
            userMapper.toUserDtoEx(user),
            user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet())));
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
