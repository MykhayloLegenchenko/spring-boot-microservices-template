package com.example.common.data.jpa.repository.support;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * Replacement for {@link JpaRepository}.
 *
 * <p>Inspired by this <a
 * href="https://vladmihalcea.com/basejparepository-hypersistence-utils/"/>article</a>
 */
@NoRepositoryBean
public interface ExtendedJpaRepository<T, ID>
    extends Repository<T, ID>, QueryByExampleExecutor<T>, JpaSpecificationExecutor<T> {

  /**
   * An alternative to {@link JpaSpecificationExecutor#findOne(Specification)} that eliminates
   * "HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory"
   * warning for join fetch.
   */
  Optional<T> fetchOne(Specification<T> spec);

  /**
   * Persists an entity.
   *
   * @param entity must not be {@literal null}.
   */
  void persist(T entity);

  /**
   * Persists an entity and flushes changes instantly.
   *
   * @param entity must not be {@literal null}.
   */
  void persistAndFlush(T entity);

  /**
   * Persists all entities.
   *
   * @param entities entities to be persisted. Must not be {@literal null}.
   */
  void persistAll(Iterable<T> entities);

  /**
   * Persists all entities and flushes changes instantly.
   *
   * @param entities entities to be persisted. Must not be {@literal null}
   */
  void persistAllAndFlush(Iterable<T> entities);

  /**
   * Merges an entity.
   *
   * @param entity entity to be merged. Must not be {@literal null}.
   * @return the merged entity; will never be {@literal null}
   */
  <S extends T> S merge(S entity);

  /**
   * Merges an entity and flushes changes instantly.
   *
   * @param entity entity to be merged. Must not be {@literal null}
   * @return the merged entity; will never be {@literal null}
   */
  <S extends T> S mergeAndFlush(S entity);

  /**
   * Merges all entities.
   *
   * @param entities entities to be merged. Must not be {@literal null}
   * @return the merged entities
   */
  <S extends T> List<S> mergeAll(Iterable<S> entities);

  /**
   * Merges all entities and flushes changes instantly.
   *
   * @param entities entities to be merged. Must not be {@literal null}
   * @return the merged entities
   */
  <S extends T> List<S> mergeAllAndFlush(Iterable<S> entities);

  /**
   * Flushes all pending changes to the database.
   *
   * @see JpaRepository#flush()
   */
  void flush();

  /**
   * Retrieves an entity by its id.
   *
   * @see JpaRepository#findById(Object)
   */
  Optional<T> findById(ID id);

  /**
   * Returns all instances of the type T with the given IDs.
   *
   * @see JpaRepository#findAllById(Iterable)
   */
  List<T> findAllById(Iterable<ID> ids);

  /**
   * Returns a reference to the entity with the given identifier.
   *
   * @see JpaRepository#getReferenceById(Object)
   */
  T getReferenceById(ID id);

  /**
   * Returns whether an entity with the given id exists.
   *
   * @see JpaRepository#existsById(Object)
   */
  boolean existsById(ID id);

  /**
   * Returns the number of entities available.
   *
   * @see JpaRepository#count()
   */
  long count();

  /**
   * Deletes a given entity.
   *
   * @see JpaRepository#delete(Object)
   */
  void delete(T entity);

  /**
   * Deletes the entity with the given id.
   *
   * @see JpaRepository#deleteById(Object)
   */
  void deleteById(ID id);

  /**
   * Deletes the given entities in a batch which means it will create a single query.
   *
   * @see JpaRepository#deleteAllInBatch(Iterable)
   */
  void deleteAllInBatch(Iterable<T> entities);

  /**
   * Deletes the entities identified by the given ids using a single query.
   *
   * @see JpaRepository#deleteAllByIdInBatch(Iterable)
   */
  void deleteAllByIdInBatch(Iterable<ID> ids);
}
