package com.example.annotation.processor;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/** The interface for an annotation element processor. */
interface ElementProcessor {

  /**
   * Returns the names of the annotation interfaces supported by this processor.
   *
   * @see Processor#getSupportedOptions()
   */
  Set<String> getSupportedAnnotationTypes();

  /**
   * Processes the given element.
   *
   * @param context annotation processor context
   * @param annotation annotation element
   * @param element element to process
   */
  void process(AnnotationProcessorContext context, TypeElement annotation, Element element);
}
