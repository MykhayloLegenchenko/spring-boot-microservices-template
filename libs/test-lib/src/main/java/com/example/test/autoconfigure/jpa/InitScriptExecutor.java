package com.example.test.autoconfigure.jpa;

import com.example.test.data.jpa.TestJpaUtils;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

/**
 * Auto-configuration that executes SQL script {@code db/init.sql} from resources if present.
 *
 * <p>Ensures one-time script execution.
 */
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnBean(EntityManagerFactory.class)
@RequiredArgsConstructor
public class InitScriptExecutor implements InitializingBean {
  private static boolean initScriptExecuted = false;

  private final EntityManagerFactory emf;

  @Override
  public void afterPropertiesSet() {
    executeInitScript(emf);
  }

  private static void executeInitScript(EntityManagerFactory emf) {
    if (initScriptExecuted) {
      return;
    }

    var resource = new ClassPathResource("db/init.sql");
    if (resource.exists()) {
      TestJpaUtils.executeSqlScript(emf, resource);
    }

    initScriptExecuted = true;
  }
}
