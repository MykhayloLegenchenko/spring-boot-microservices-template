package com.example.annotation.processor;

import com.example.annotation.annotation.ClientInterface;
import com.google.auto.service.AutoService;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/** Annotation processor for the {@link ClientInterface} annotation. */
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
  private static final Set<ElementProcessor> elementProcessors =
      Set.of(ClientInterfaceProcessor.INSTANCE);

  private static final Set<String> supportedAnnotationTypes =
      elementProcessors.stream()
          .map(ElementProcessor::getSupportedAnnotationTypes)
          .flatMap(Set::stream)
          .collect(Collectors.toUnmodifiableSet());

  private AnnotationProcessorContext context;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    context = new AnnotationProcessorContext(processingEnv);
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return supportedAnnotationTypes;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_23;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return false;
    }

    for (var annotation : annotations) {
      var processors = getProcessors(annotation);

      for (var element : getElements(roundEnv, annotation)) {
        for (var processor : processors) {
          processor.process(context, annotation, element);
        }
      }
    }

    return false;
  }

  private static Set<? extends Element> getElements(
      RoundEnvironment roundEnv, TypeElement annotation) {
    return annotation.getKind() == ElementKind.ANNOTATION_TYPE
        ? roundEnv.getElementsAnnotatedWith(annotation)
        : Collections.emptySet();
  }

  private static Set<ElementProcessor> getProcessors(TypeElement annotation) {
    var annotationType = annotation.getQualifiedName().toString();
    return elementProcessors.stream()
        .filter(processor -> processor.getSupportedAnnotationTypes().contains(annotationType))
        .collect(Collectors.toUnmodifiableSet());
  }
}
