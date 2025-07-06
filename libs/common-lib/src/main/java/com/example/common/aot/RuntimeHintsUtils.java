package com.example.common.aot;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.core.DecoratingProxy;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/** Utility class for managing runtime hints during native image generation. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuntimeHintsUtils {

  /**
   * Registers the declared constructors of the given types for reflection-based access during
   * native image generation.
   *
   * <p>This includes declared constructors to ensure compatibility with frameworks like Jackson or
   * other serialization libraries that rely on reflection.
   *
   * @param hints the {@link RuntimeHints} instance used to register reflection metadata
   * @param types the array of {@link Class} objects whose constructors should be registered
   */
  public static void registerConstructors(RuntimeHints hints, Class<?>... types) {
    var reflection = hints.reflection();
    for (var type : types) {
      reflection.registerType(type, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
  }

  /**
   * Registers the specified DTO classes for reflection access during native image generation.
   *
   * <p>This includes public constructors, methods, and declared fields to ensure compatibility with
   * frameworks like Jackson or other serialization libraries that rely on reflection.
   *
   * @param hints the {@link RuntimeHints} instance used to register reflection metadata
   * @param types the DTO {@link Class} types to register
   */
  public static void registerDto(RuntimeHints hints, Class<?>... types) {
    var reflection = hints.reflection();
    for (var type : types) {
      reflection.registerType(
          type,
          hint ->
              hint.withMembers(
                  MemberCategory.INVOKE_DECLARED_METHODS,
                  MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                  MemberCategory.DECLARED_FIELDS));
    }
  }

  /**
   * Registers all concrete DTO classes (including records) located in the specified base package
   * for reflection-based access during AOT or native image compilation.
   *
   * <p>This method scans the {@code basePackage} and registers each non-abstract, non-interface
   * class (including Java records) using the provided {@link RuntimeHints} instance, enabling
   * reflection for serialization, deserialization, and other runtime access.
   *
   * @param hints the {@link RuntimeHints} instance used to register reflection hints
   * @param basePackage the root package to scan for DTO classes
   */
  public static void registerDto(RuntimeHints hints, String basePackage) {
    Class<?>[] dtoClasses;
    try (var scanResult = new ClassGraph().enableClassInfo().acceptPackages(basePackage).scan()) {
      dtoClasses =
          scanResult
              .getAllStandardClasses()
              .filter(Predicate.not(ClassInfo::isAbstract)::test)
              .loadClasses()
              .toArray(Class[]::new);
    }

    registerDto(hints, dtoClasses);
  }

  /**
   * Registers the specified interface types for JDK dynamic proxy generation to support {@link
   * HttpServiceProxyFactory#createClient(Class)}.
   *
   * <p>This is required when building a native image, as GraalVM must be explicitly instructed to
   * allow proxying of interfaces. It also includes standard Spring proxy interfaces required by the
   * proxy factory.
   *
   * @param hints the {@link RuntimeHints} container used to register proxy hints
   * @param types the interface {@link Class} types that will be proxied
   */
  public static void registerClientInterface(RuntimeHints hints, Class<?>... types) {
    for (var type : types) {
      hints
          .proxies()
          .registerJdkProxy(type, SpringProxy.class, Advised.class, DecoratingProxy.class);
    }
  }

  /**
   * Registers runtime hints for a client API, including both blocking and reactive interfaces, as
   * well as all DTO classes located under the specified base package.
   *
   * <p>This method internally registers:
   *
   * <ul>
   *   <li>The provided {@code blockingClient} and {@code reactiveClient} interfaces using {@link
   *       #registerClientInterface(RuntimeHints, Class[])}.
   *   <li>All concrete classes (including records) in the {@code basePackage} using {@link
   *       #registerDto(RuntimeHints, Class[])}.
   * </ul>
   *
   * @param hints the {@link RuntimeHints} instance to register hints into
   * @param blockingClient the interface representing the blocking HTTP client
   * @param reactiveClient the interface representing the reactive HTTP client
   * @param basePackage the base package to scan for DTO classes
   */
  public static void registerClientApi(
      RuntimeHints hints, Class<?> blockingClient, Class<?> reactiveClient, String basePackage) {

    registerClientInterface(hints, blockingClient, reactiveClient);
    registerDto(hints, basePackage);
  }
}
