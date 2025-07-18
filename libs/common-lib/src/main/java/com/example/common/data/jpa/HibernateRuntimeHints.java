package com.example.common.data.jpa;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.util.function.Predicate;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of Hibernate. */
public class HibernateRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    registerConstructors(hints, "com.example.common.data.jpa.jdbc", JdbcType.class);
    registerConstructors(hints, "com.example.common.data.jpa.naming", PhysicalNamingStrategy.class);
    registerMetamodels(hints);
  }

  private static void registerConstructors(
      RuntimeHints hints, String basePackage, Class<?> parentType) {

    var reflection = hints.reflection();
    try (var scanResult = new ClassGraph().enableClassInfo().acceptPackages(basePackage).scan()) {
      for (var type :
          scanResult
              .getAllStandardClasses()
              .filter(Predicate.not(ClassInfo::isAbstract)::test)
              .loadClasses()) {

        if (parentType.isAssignableFrom(type)) {
          reflection.registerType(type, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        }
      }
    }
  }

  private static void registerMetamodels(RuntimeHints hints) {
    var reflection = hints.reflection();
    try (var scanResult = new ClassGraph().enableAnnotationInfo().scan()) {
      for (var type :
          scanResult
              .getClassesWithAnnotation(StaticMetamodel.class)
              .filter(ClassInfo::isStandardClass)
              .loadClasses()) {
        reflection.registerType(type, MemberCategory.DECLARED_FIELDS);
      }
    }
  }
}
