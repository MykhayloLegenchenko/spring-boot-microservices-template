package com.example.annotation.processor;

import static com.sun.tools.javac.tree.JCTree.JCClassDecl;
import static com.sun.tools.javac.tree.JCTree.JCExpression;
import static com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import static com.sun.tools.javac.tree.JCTree.JCModifiers;
import static com.sun.tools.javac.tree.JCTree.JCTypeApply;

import com.example.annotation.annotation.ClientInterface;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleElementVisitor9;
import javax.tools.Diagnostic;

/** {@link ElementProcessor} that processes {@link ClientInterface} annotation. */
class ClientInterfaceProcessor {
  public static final ElementProcessor INSTANCE = new ElementProcessorImpl();

  private static final String MONO_TYPE = "reactor.core.publisher.Mono";
  private static final String FLUX_TYPE = "reactor.core.publisher.Flux";
  private static final Set<String> requiredTypeAnnotations = Set.of("HttpExchange");
  private static final Set<String> requiredMethodAnnotations =
      Set.of("DeleteExchange", "GetExchange", "PatchExchange", "PostExchange", "PutExchange");

  private static final Set<String> namedAnnotations =
      Stream.of("RequestParam", "PathVariable")
          .map("org.springframework.web.bind.annotation."::concat)
          .collect(Collectors.toUnmodifiableSet());

  private final AnnotationProcessorContext processorContext;
  private final TypeElement rootAnnotation;
  private final Element rootElement;
  private boolean hasErrors;
  private boolean hasMethods;
  private ServiceType serviceType;
  private final Set<String> imports;

  private ClientInterfaceProcessor(
      AnnotationProcessorContext processorContext,
      TypeElement rootAnnotation,
      Element rootElement) {

    this.processorContext = processorContext;
    this.rootAnnotation = rootAnnotation;
    this.rootElement = rootElement;
    this.imports = new HashSet<>();
  }

  private void process() {
    if (rootElement.getKind() != ElementKind.INTERFACE) {
      printError("Annotation target is not an interface", rootElement, null);
      return;
    }

    if (rootElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      printError("Nested interfaces is not supported", rootElement, null);
      return;
    }

    checkRequiredAnnotations(rootElement, requiredTypeAnnotations, "Interface {0}");
    if (hasErrors) {
      return;
    }

    var origClassDecl = (JCClassDecl) processorContext.getTree(rootElement);
    var clasDecl = createClassDecl(origClassDecl);

    if (hasErrors) {
      return;
    } else if (!hasMethods) {
      printError("The interface must contain at least one method", rootElement, null);
      return;
    }

    try {
      generateSource(clasDecl, origClassDecl);
    } catch (IOException e) {
      printError("{0}: {1}", rootElement, null, e.getClass().getName(), e.getMessage());
    }
  }

  private static class ElementProcessorImpl implements ElementProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
      return Set.of(ClientInterface.class.getName());
    }

    @Override
    public void process(
        AnnotationProcessorContext context, TypeElement annotation, Element element) {

      new ClientInterfaceProcessor(context, annotation, element).process();
    }
  }

  private enum ServiceType {
    BLOCKING,
    REACTIVE
  }

  private JCClassDecl createClassDecl(JCClassDecl origClassDecl) {
    var defs =
        com.sun.tools.javac.util.List.from(
            rootElement.getEnclosedElements().stream().map(this::processElement).toList());

    return processorContext
        .getTreeMaker()
        .ClassDef(
            getClassModifiers(origClassDecl),
            createClassName(origClassDecl),
            origClassDecl.typarams,
            origClassDecl.extending,
            origClassDecl.implementing,
            origClassDecl.permitting,
            defs);
  }

  private Name createClassName(JCClassDecl classDef) {
    String newSuffix;
    String oldSuffix;

    if (serviceType == ServiceType.BLOCKING) {
      newSuffix = "Reactive";
      oldSuffix = "Blocking";
    } else {
      newSuffix = "Blocking";
      oldSuffix = "Reactive";
    }

    var name = classDef.getSimpleName().toString();
    if (name.endsWith("Client")) {
      name = name.substring(0, name.length() - 6);
    }

    if (name.endsWith(oldSuffix)) {
      name = name.substring(0, name.length() - oldSuffix.length());
    }

    return processorContext.getNames().fromString(name + newSuffix + "Client");
  }

  private void generateSource(JCClassDecl clasDef, JCClassDecl origClassDecl) throws IOException {

    var packageName = rootElement.getEnclosingElement();
    var sourceFile =
        processorContext
            .getProcessingEnv()
            .getFiler()
            .createSourceFile(packageName + "." + clasDef.name, rootElement);

    try (var out = new OutputStreamWriter(sourceFile.openOutputStream(), StandardCharsets.UTF_8)) {
      out.write("package " + packageName + ";\n\n");

      for (var imp : processorContext.getPath(rootElement).getCompilationUnit().getImports()) {
        var type = imp.getQualifiedIdentifier().toString();
        if (!MONO_TYPE.equals(type)
            && !FLUX_TYPE.equals(type)
            && !ClientInterface.class.getName().equals(type)) {

          imports.remove(type);

          out.write("import " + (imp.isStatic() ? "static " : "") + type + ";\n");
        }
      }

      for (var imp : imports) {
        out.write("import " + imp + ";\n");
      }

      out.write(
          MessageFormat.format(
              """

                        @{0}(
                            value="{1}",
                            date="{2}",
                            comments = "source: {3}.{4}"
                        )
                        """,
              Generated.class.getName(),
              getClass().getName(),
              Instant.now(),
              packageName,
              origClassDecl.name));

      out.write(clasDef.toString().substring(1));
    }
  }

  private JCModifiers getClassModifiers(JCClassDecl origClassDef) {
    return processorContext
        .getTreeMaker()
        .Modifiers(
            origClassDef.mods.flags,
            com.sun.tools.javac.util.List.from(
                origClassDef.mods.annotations.stream()
                    .filter(a -> !ClientInterface.class.getName().equals(a.type.toString()))
                    .toList()));
  }

  private JCTree processElement(Element el) {
    return el.accept(
        new SimpleElementVisitor9<JCTree, Void>() {
          @Override
          protected JCTree defaultAction(Element e, Void p) {
            return processMember(e);
          }

          @Override
          public JCTree visitExecutable(ExecutableElement e, Void p) {
            return processMethod(e);
          }
        },
        null);
  }

  private JCTree processMember(Element el) {
    if (el.getKind() != ElementKind.FIELD) {
      printError("The interface must contain only methods and fields", el, null);
    }

    return (JCTree) processorContext.getTree(el);
  }

  private JCTree processMethod(ExecutableElement method) {
    hasMethods = true;
    checkRequiredAnnotations(method, requiredMethodAnnotations, "Method {0}");

    var methodDef = (JCMethodDecl) processorContext.getTree(method);
    return processorContext
        .getTreeMaker()
        .MethodDef(
            methodDef.getModifiers(),
            methodDef.name,
            createMethodResult(method, methodDef.restype),
            methodDef.typarams,
            methodDef.params,
            methodDef.thrown,
            methodDef.body,
            methodDef.defaultValue);
  }

  private JCExpression createMethodResult(ExecutableElement method, JCExpression oldResult) {
    checkMethod(method);

    if (method.getReturnType() instanceof DeclaredType t && (isMono(t) || isFlux(t))) {
      checkServiceType(ServiceType.REACTIVE, method);
      return createBlockingMethodResult(method, oldResult);
    }

    checkServiceType(ServiceType.BLOCKING, method);
    return createReactiveMethodResult(method, oldResult);
  }

  private JCExpression createBlockingMethodResult(
      ExecutableElement method, JCExpression oldResult) {
    if (method.getReturnType() instanceof DeclaredType declaredType
        && declaredType.getTypeArguments().size() == 1) {

      var typeArgument = ((JCTypeApply) oldResult).arguments.getFirst();

      if (isFlux(declaredType)) {
        imports.add(List.class.getName());
        return getGenericMethodResult(List.class.getName(), typeArgument);

      } else if (isMono(declaredType)) {
        var unboxedSymbol = processorContext.getUnboxedSymbol(typeArgument.type);
        return unboxedSymbol != null ? processorContext.getIdent(unboxedSymbol) : typeArgument;
      }
    }

    printError("Method must return parametrized Mono or Flux", method, null);
    return oldResult;
  }

  private JCExpression createReactiveMethodResult(
      ExecutableElement method, JCExpression oldResult) {
    String className;
    JCExpression typeArgument;

    if (method.getReturnType() instanceof DeclaredType declaredType
        && "java.util.List".equals(declaredType.asElement().toString())) {
      className = FLUX_TYPE;
      typeArgument = ((JCTypeApply) oldResult).arguments.getFirst();
    } else {
      className = MONO_TYPE;
      var boxedSymbol = processorContext.getBoxedSymbol(oldResult.type);
      if (boxedSymbol != null) {
        typeArgument = processorContext.getIdent(boxedSymbol);
      } else {
        typeArgument = oldResult;
      }
    }

    imports.add(className);
    return getGenericMethodResult(className, typeArgument);
  }

  private JCTypeApply getGenericMethodResult(String className, JCExpression typeArgument) {
    return processorContext
        .getTreeMaker()
        .TypeApply(
            processorContext.getIdent(className), com.sun.tools.javac.util.List.of(typeArgument));
  }

  private static boolean isFlux(DeclaredType type) {
    return FLUX_TYPE.equals(type.asElement().toString());
  }

  private static boolean isMono(DeclaredType type) {
    return MONO_TYPE.equals(type.asElement().toString());
  }

  private void checkServiceType(ServiceType type, ExecutableElement method) {
    if (serviceType == null) {
      serviceType = type;
    } else if (serviceType != type) {
      var message =
          serviceType == ServiceType.REACTIVE
              ? "Reactive interface methods must return Mono or Flux"
              : "Blocking interface methods cannot return Mono or Flux";

      printError(message, method, null);
    }
  }

  private void checkMethod(ExecutableElement method) {
    var modifiers = method.getModifiers();
    if (modifiers.size() != 2
        || (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.ABSTRACT))) {
      printError("Method should be public abstract", method, null);
    }

    for (var param : method.getParameters()) {
      for (var annotation : getAnnotationMirrors(param, namedAnnotations)) {
        checkNamedAnnotation(annotation, param);
      }
    }
  }

  private void checkNamedAnnotation(AnnotationMirror annotation, VariableElement param) {
    if (annotation.getElementValues().entrySet().stream()
        .noneMatch(
            entry -> {
              var name = entry.getKey().getSimpleName().toString();
              return ("name".equals(name) || "value".equals(name))
                  && !"\"\"".equals(entry.getValue().toString());
            })) {
      printError(
          "Annotation @{0} must have non-empty \"name\" or \"value\" attribute",
          param, annotation, annotation.getAnnotationType().asElement().getSimpleName());
    }
  }

  private void checkRequiredAnnotations(Element el, Set<String> annotations, String message) {
    if (getAnnotationMirror(
            el,
            annotations.stream()
                .map("org.springframework.web.service.annotation."::concat)
                .collect(Collectors.toUnmodifiableSet()))
        == null) {
      printError(
          message,
          el,
          null,
          "must have "
              + (annotations.size() > 1
                  ? "one of " + annotations.stream().map("@"::concat).toList() + " annotations"
                  : "@" + annotations.stream().findFirst().orElse(null) + " annotation"));
    }
  }

  private void printError(
      String message, Element el, AnnotationMirror annotation, Object... arguments) {
    hasErrors = true;
    processorContext
        .getProcessingEnv()
        .getMessager()
        .printMessage(
            Diagnostic.Kind.ERROR,
            getClass().getSimpleName() + ": " + MessageFormat.format(message, arguments),
            el,
            annotation != null
                ? annotation
                : getAnnotationMirror(el, List.of(rootAnnotation.getQualifiedName().toString())));
  }

  private static List<? extends AnnotationMirror> getAnnotationMirrors(
      Element el, Collection<String> annotationTypes) {

    return el != null
        ? el.getAnnotationMirrors().stream()
            .filter(a -> annotationTypes.contains(a.getAnnotationType().asElement().toString()))
            .toList()
        : List.of();
  }

  private static AnnotationMirror getAnnotationMirror(
      Element el, Collection<String> annotationTypes) {

    return getAnnotationMirrors(el, annotationTypes).stream().findFirst().orElse(null);
  }
}
