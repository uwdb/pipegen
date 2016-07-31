package org.brandonhaynes.pipegen.optimization.sinks;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.StringConstant;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.brandonhaynes.pipegen.utilities.ClassUtilities.getClassHierarchyMethods;
import static org.brandonhaynes.pipegen.utilities.ClassUtilities.isSameSubSignature;

public class InvokeMethodSinkExpression implements SinkExpression {
    private final Set<SootMethod> methods;

    public InvokeMethodSinkExpression(Scene scene, Class<?> clazz, String methodName, List<Type> types) {
        this(scene.getSootClass(clazz.getName()).getMethod(methodName, types));
    }

    public InvokeMethodSinkExpression(Scene scene, Class<?> clazz, String methodNamePattern) {
        this(scene.getSootClass(clazz.getName()), Pattern.compile(methodNamePattern));
    }

    public InvokeMethodSinkExpression(SootClass clazz, Pattern methodNamePattern) {
        this(getClassHierarchyMethods(clazz).filter(m -> methodNamePattern.matcher(m.getName()).matches())
                                            .collect(Collectors.toList()));
    }

    public InvokeMethodSinkExpression(SootMethod method) {
        this(Lists.newArrayList(method));
    }

    public InvokeMethodSinkExpression(Collection<SootMethod> methods) {
        //TODO add tests for calling base function (see Derby ExportAbstract.writeData)
        this.methods = methods.stream().flatMap(m -> getClassHierarchyMethods(m.getDeclaringClass())
                                       .filter(hm -> isSameSubSignature(m, hm)))
                                       .collect(Collectors.toSet());
    }

    @Override
    public boolean isApplicable(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                                Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        return node instanceof InvokeStmt && isApplicable((InvokeStmt) node);
    }

    private boolean isApplicable(InvokeStmt node) {
        SootMethod method = node.getInvokeExpr().getMethod();
        return methods.stream().anyMatch(m -> isSameSubSignature(method, m));
    }

    @Override
    public void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                               Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        assert(isApplicable(graph, input, node, output, taintedValues, queue, processed));

        if(node instanceof InvokeStmt)
            taintedValues.addAll(getTaintedValues((InvokeStmt)node));
    }

    private Set<Value> getTaintedValues(InvokeStmt node) {
        return node.getInvokeExpr().getArgs().stream().filter(this::isStringLike).collect(Collectors.toSet());
    }

    private boolean isStringLike(Value value) {
        return (value.getType() instanceof RefType &&
                !(value instanceof StringConstant) &&
                isStringLike((RefType)value.getType())) ||
                (value.getType() instanceof ArrayType &&
                 isStringArrayLike((ArrayType)value.getType()));
    }

    private boolean isStringArrayLike(ArrayType type) {
        return type.baseType instanceof RefType && isStringLike((RefType)type.baseType);
    }

    private boolean isStringLike(RefType type) {
        //TODO how to generalize this?
        return type.getClassName().equals(Object.class.getName()) ||
               // Remove these?
               type.getClassName().equals(String[].class.getName()) ||
               type.getClassName().equals(Object[].class.getName()) ||
               type.getClassName().equals(String.class.getName()) ||
               type.getClassName().equals(CharSequence.class.getName()) ||
               type.getClassName().equals(Iterable.class.getName());
    }
}
