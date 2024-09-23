package com.example.common.data.jpa.repository.support;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/** Implementation class for {@link ExtendedJpaRepository} interface */
public class ExtendedJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
    implements ExtendedJpaRepository<T, ID> {

  private final EntityManager entityManager;

  @Override
  @Transactional(readOnly = true)
  public Optional<T> fetchOne(Specification<T> spec) {
    var result = findAll(spec);
    return Optional.ofNullable(result.isEmpty() ? null : result.getFirst());
  }

  public ExtendedJpaRepositoryImpl(
      JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);

    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public void persist(T entity) {
    Assert.notNull(entity, "Entity must not be null");

    entityManager.persist(entity);
  }

  @Override
  @Transactional
  public void persistAndFlush(T entity) {
    persist(entity);
    flush();
  }

  @Override
  @Transactional
  public void persistAll(Iterable<T> entities) {
    Assert.notNull(entities, "Entities must not be null");

    for (T entity : entities) {
      persist(entity);
    }
  }

  @Override
  @Transactional
  public void persistAllAndFlush(Iterable<T> entities) {
    persistAll(entities);
    flush();
  }

  @Override
  @Transactional
  public <S extends T> S merge(S entity) {
    Assert.notNull(entity, "Entity must not be null");

    return entityManager.merge(entity);
  }

  @Override
  @Transactional
  public <S extends T> S mergeAndFlush(S entity) {
    var result = merge(entity);
    flush();

    return result;
  }

  @Override
  @Transactional
  public <S extends T> List<S> mergeAll(Iterable<S> entities) {
    Assert.notNull(entities, "Entities must not be null");

    var result = new ArrayList<S>();
    for (S entity : entities) {
      result.add(merge(entity));
    }

    return result;
  }

  @Override
  @Transactional
  public <S extends T> List<S> mergeAllAndFlush(Iterable<S> entities) {
    var result = mergeAll(entities);
    flush();

    return result;
  }
}
