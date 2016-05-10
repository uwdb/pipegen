package org.brandonhaynes.pipegen.optimization.sinks;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;

import java.util.Set;
import java.util.regex.Pattern;

public class InvokeMethodSinkExpression implements SinkExpression {
    private final Pattern methodNamePattern;
    private final Class<?> clazz;

    public InvokeMethodSinkExpression(Class<?> clazz, String methodName) {
        this(clazz, Pattern.compile(methodName));
    }

    public InvokeMethodSinkExpression(Class<?> clazz, Pattern methodNamePattern) {
        this.clazz = clazz;
        this.methodNamePattern = methodNamePattern;
    }

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return node instanceof InvokeStmt && isApplicable(input, (InvokeStmt) node, output);
    }

    private boolean isApplicable(Set<Unit> input, InvokeStmt node, Set<Unit> output) {
        SootMethod method = node.getInvokeExpr().getMethod();
        return clazz.getName().equals(method.getDeclaringClass().getName()) &&
               methodNamePattern.matcher(method.getName()).matches();
    }
}
