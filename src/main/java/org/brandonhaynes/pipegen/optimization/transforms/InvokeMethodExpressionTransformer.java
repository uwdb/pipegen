package org.brandonhaynes.pipegen.optimization.transforms;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;

import java.util.Set;
import java.util.regex.Pattern;

public class InvokeMethodExpressionTransformer implements ExpressionTransformer {
    private final Pattern methodNamePattern;
    private final Class<?> clazz;

    public InvokeMethodExpressionTransformer(Class<?> clazz, String methodName) {
        this(clazz, Pattern.compile(methodName));
    }

    public InvokeMethodExpressionTransformer(Class<?> clazz, Pattern methodNamePattern) {
        this.clazz = clazz;
        this.methodNamePattern = methodNamePattern;
    }

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return node instanceof Stmt && isApplicable(input, (Stmt) node, output);
    }

    private boolean isApplicable(Set<Unit> input, Stmt node, Set<Unit> output) {
        return node.containsInvokeExpr() && isApplicable(input, node.getInvokeExpr(), output);
    }

    private boolean isApplicable(Set<Unit> input, InvokeExpr node, Set<Unit> output) {
        SootMethod method = node.getMethod();
        return method != null &&
               !input.isEmpty() &&
               clazz.getName().equals(method.getDeclaringClass().getName()) &&
               methodNamePattern.matcher(method.getName()).matches();
    }

    @Override
    public void transform(Set<Unit> input, Unit node, Set<Unit> output) {
        if(!(node instanceof Stmt) || !((Stmt)node).containsInvokeExpr())
            throw new RuntimeException("Expected statement.");
        else
            transform(input, (Stmt)node, output);
    }

    private void transform(Set<Unit> input, Stmt statement, Set<Unit> output) {
        ValueBox invocationBox = statement.getInvokeExprBox();
        Value virtualInstanceReference = ((JVirtualInvokeExpr)invocationBox.getValue()).getBase();

        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(AugmentedString.class.getName())
                .getMethod("decorate", Lists.newArrayList(Scene.v().getObjectType()))
                .makeRef();
        InvokeExpr newInvocation = new JStaticInvokeExpr(newMethodRef, Lists.newArrayList(virtualInstanceReference));

        invocationBox.setValue(newInvocation);
    }
}
