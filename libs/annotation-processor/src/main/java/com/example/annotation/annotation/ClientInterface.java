package com.example.annotation.annotation;

import com.example.annotation.processor.AnnotationProcessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @ClientInterface} annotation generates reactive versions for blocking client
 * interfaces and blocking versions for reactive client interfaces.
 *
 * <p>Source (blocking):
 *
 * <pre>{@literal
 * @ClientInterface
 * @HttpExchange("/api/v1/test")
 * public interface TestBlockingClient {
 *   @GetExchange("/one")
 *   int getInteger(@RequestParam("name") String name);
 *
 *   @GetExchange("/list")
 *   List@<String> getList(@RequestParam("name") String name);
 *
 *   @PostExchange("/remove")
 *   void remove(@RequestBody String name);
 * }
 * }</pre>
 *
 * Generated:
 *
 * <pre>{@literal
 * @HttpExchange("/api/v1/test")
 * public interface TestReactiveClient {
 *     @GetExchange("/one")
 *     Mono<Integer> getInteger(@RequestParam("name")
 *     String name);
 *
 *     @GetExchange("/list")
 *     Flux@<String> getList(@RequestParam("name")
 *     String name);
 *
 *     @PostExchange("/remove")
 *     Mono@<Void&gt remove(@RequestBody
 *     String name);
 * }
 * }</pre>
 *
 * <p>Source (reactive):
 *
 * <pre>{@literal
 * @ClientInterface
 * @HttpExchange("/api/v1/test")
 * public interface TestReactiveClient {
 *   @GetExchange("/one")
 *   Mono@<Integer> getInteger(@RequestParam("name") String name);
 *
 *   @GetExchange("/list")
 *   Flux@<String> getList(@RequestParam("name") String name);
 *
 *   @PostExchange("/remove")
 *   Mono@<Void> remove(@RequestBody String name);
 * }
 * }</pre>
 *
 * Generated:
 *
 * <pre>{@literal
 * @HttpExchange("/api/v1/test")
 * public interface TestBlockingClient {
 *   @GetExchange("/one")
 *   int getInteger(@RequestParam("name") String name);
 *
 *   @GetExchange("/list")
 *   List@<String> getList(@RequestParam("name") String name);
 *
 *   @PostExchange("/remove")
 *   void remove(@RequestBody String name);
 * }
 * }</pre>
 *
 * <p>Handled by {@link AnnotationProcessor}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ClientInterface {}
