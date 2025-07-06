package com.example.common.data.liquibase;

import com.example.common.aot.RuntimeHintsUtils;
import liquibase.changelog.FastCheckService;
import liquibase.changelog.visitor.ValidatingVisitorGeneratorFactory;
import liquibase.database.LiquibaseTableNamesFactory;
import liquibase.parser.SqlParserFactory;
import liquibase.report.ShowSummaryGeneratorFactory;
import liquibase.ui.LoggerUIService;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of Liquibase. */
public class LiquibaseRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    RuntimeHintsUtils.registerConstructors(
        hints,
        FastCheckService.class,
        FastCheckService.class,
        ValidatingVisitorGeneratorFactory.class,
        LiquibaseTableNamesFactory.class,
        ShowSummaryGeneratorFactory.class,
        SqlParserFactory.class,
        LoggerUIService.class);
  }
}
