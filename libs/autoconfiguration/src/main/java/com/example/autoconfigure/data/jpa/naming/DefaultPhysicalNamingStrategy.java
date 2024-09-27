package com.example.autoconfigure.data.jpa.naming;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Default {@link PhysicalNamingStrategy} implementation. Extends
 * CamelCaseToUnderscoresNamingStrategy, removes "Entity" suffix from table names.
 */
public class DefaultPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {
  private static final String ENTITY_SUFFIX = "Entity";

  @Override
  public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
    var name = logicalName.getText();
    if (logicalName.isQuoted()) {
      name = Identifier.unQuote(name);
    }

    if (name.endsWith(ENTITY_SUFFIX)) {
      name = name.substring(0, name.length() - ENTITY_SUFFIX.length());
      if (logicalName.isQuoted()) {
        var origText = logicalName.getText();
        name = origText.charAt(0) + name + origText.charAt(origText.length() - 1);
      }

      logicalName = new Identifier(name, logicalName.isQuoted());
    }

    return super.toPhysicalTableName(logicalName, jdbcEnvironment);
  }
}
