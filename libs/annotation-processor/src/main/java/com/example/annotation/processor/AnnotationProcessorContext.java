package com.example.annotation.processor;

import static com.sun.tools.javac.tree.JCTree.JCIdent;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

/** Holder object for the annotation processor context. */
@Value
class AnnotationProcessorContext {
  private static Map<TypeKind, String> boxingMap = createBoxingMap();

  ProcessingEnvironment processingEnv;
  Trees trees;
  Names names;
  Elements elements;
  TreeMaker treeMaker;

  @Getter(AccessLevel.NONE)
  Map<String, Symbol> unboxingMap;

  public AnnotationProcessorContext(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;

    trees = Trees.instance(processingEnv);

    var context = ((JavacProcessingEnvironment) processingEnv).getContext();
    names = Names.instance(context);
    treeMaker = TreeMaker.instance(context);

    elements = processingEnv.getElementUtils();

    unboxingMap = creatUnboxingMap(Symtab.instance(context));
  }

  public Tree getTree(Element element) {
    return trees.getTree(element);
  }

  public TreePath getPath(Element element) {
    return trees.getPath(element);
  }

  public Symbol getUnboxedSymbol(Type type) {
    return unboxingMap.get(type.asElement().getQualifiedName().toString());
  }

  public Symbol getBoxedSymbol(Type type) {
    var boxedType = boxingMap.get(type.getKind());
    return boxedType != null ? getClassSymbol(boxedType) : null;
  }

  public Symbol getClassSymbol(String className) {
    return (Symbol) processingEnv.getElementUtils().getTypeElement(className);
  }

  public JCIdent getIdent(Symbol sym) {
    return treeMaker.Ident(sym);
  }

  public JCIdent getIdent(String className) {
    return getIdent(getClassSymbol(className));
  }

  private static Map<String, Symbol> creatUnboxingMap(Symtab symtab) {
    return Map.of(
        Integer.class.getName(),
        symtab.intType.tsym,
        Character.class.getName(),
        symtab.charType.tsym,
        Byte.class.getName(),
        symtab.byteType.tsym,
        Short.class.getName(),
        symtab.shortType.tsym,
        Long.class.getName(),
        symtab.longType.tsym,
        Float.class.getName(),
        symtab.floatType.tsym,
        Double.class.getName(),
        symtab.doubleType.tsym,
        Void.class.getName(),
        symtab.voidType.tsym);
  }

  private static Map<TypeKind, String> createBoxingMap() {
    return Map.of(
        TypeKind.INT,
        Integer.class.getName(),
        TypeKind.CHAR,
        Character.class.getName(),
        TypeKind.BYTE,
        Byte.class.getName(),
        TypeKind.SHORT,
        Short.class.getName(),
        TypeKind.LONG,
        Long.class.getName(),
        TypeKind.FLOAT,
        Float.class.getName(),
        TypeKind.DOUBLE,
        Double.class.getName(),
        TypeKind.VOID,
        Void.class.getName());
  }
}
