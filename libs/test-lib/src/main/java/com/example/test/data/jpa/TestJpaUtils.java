package com.example.test.data.jpa;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/** JPA testing utilities. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestJpaUtils {
  /**
   * Executes SQL script from resource.
   *
   * @param emf entity manager factory
   * @param resource the resource to load the SQL script from
   */
  public static void executeSqlScript(EntityManagerFactory emf, Resource resource) {
    doWorkInTransaction(emf, connection -> ScriptUtils.executeSqlScript(connection, resource));
  }

  private static void doWorkInTransaction(EntityManagerFactory emf, Work work) {
    EntityTransaction txn = null;

    try (var em = emf.createEntityManager()) {
      txn = em.getTransaction();

      txn.begin();
      em.unwrap(Session.class).doWork(work);
      txn.commit();
    } catch (Throwable e) {
      if (txn != null && txn.isActive()) {
        txn.rollback();
      }
      throw e;
    }
  }
}
