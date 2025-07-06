package com.example.autoconfigure.data.jpa;

import com.example.common.data.jpa.HibernateRuntimeHints;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ImportRuntimeHints;

/** Autoconfiguration for Hibernate. */
@AutoConfiguration
@ConditionalOnClass(SessionImplementor.class)
@ImportRuntimeHints(HibernateRuntimeHints.class)
public class HibernateJpaAutoConfiguration {}
