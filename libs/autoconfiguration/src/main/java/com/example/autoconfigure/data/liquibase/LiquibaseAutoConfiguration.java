package com.example.autoconfigure.data.liquibase;

import com.example.common.data.liquibase.LiquibaseRuntimeHints;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ImportRuntimeHints;

/** Autoconfiguration for Liquibase. */
@AutoConfiguration
@ConditionalOnClass(SpringLiquibase.class)
@ImportRuntimeHints(LiquibaseRuntimeHints.class)
public class LiquibaseAutoConfiguration {}
