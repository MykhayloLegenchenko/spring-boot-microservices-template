package com.example.common.data.liquibase;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/** Registers runtime hints for native image compatibility of Liquibase. */
public class LiquibaseRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    registerConstructors(
        hints,
        "liquibase.changelog.FastCheckService",
        "liquibase.changelog.visitor.ValidatingVisitorGeneratorFactory",
        "liquibase.database.LiquibaseTableNamesFactory",
        "liquibase.report.ShowSummaryGeneratorFactory",
        "liquibase.parser.SqlParserFactory",
        "liquibase.ui.LoggerUIService");
  }

  public static void registerConstructors(RuntimeHints hints, String... classNames) {
    var reflection = hints.reflection();
    for (var className : classNames) {
      reflection.registerType(
          TypeReference.of(className),
          hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
    }
  }
}
