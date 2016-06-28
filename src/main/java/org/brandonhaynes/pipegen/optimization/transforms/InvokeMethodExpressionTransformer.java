package org.brandonhaynes.pipegen.optimization.transforms;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InvokeMethodExpressionTransformer implements ExpressionTransformer {
    private final Pattern methodNamePattern;
    private final Class<?> targetClass;

    public InvokeMethodExpressionTransformer(Class<?> targetClass, String methodName) {
        this(targetClass, Pattern.compile(methodName));
    }

    public InvokeMethodExpressionTransformer(Class<?> targetClass, Pattern methodNamePattern) {
        this.targetClass = targetClass;
        this.methodNamePattern = methodNamePattern;
    }

    protected Class<?> getTargetClass() { return targetClass; }

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return node instanceof Stmt && isApplicable(input, (Stmt) node);
    }

    private boolean isApplicable(Set<Unit> input, Stmt node) {
        return node.containsInvokeExpr() && isApplicable(input, node.getInvokeExpr());
    }

    private boolean isApplicable(Set<Unit> input, InvokeExpr node) {
        SootMethod method = node.getMethod();
        return method != null &&
               !input.isEmpty() &&
               targetClass.getName().equals(method.getDeclaringClass().getName()) &&
               methodNamePattern.matcher(method.getName()).matches();
    }

    @Override
    public void transform(Set<Unit> input, Unit node, Set<Unit> output, CompositeExpressionTransformer transforms) {
        if(!(node instanceof Stmt) || !((Stmt)node).containsInvokeExpr())
            throw new RuntimeException("Expected statement.");
        else
            transform((Stmt)node, transforms);
    }

    protected void transform(Stmt statement, CompositeExpressionTransformer transforms) {
        ValueBox invocationBox = statement.getInvokeExprBox();

        if (invocationBox.getValue() instanceof JVirtualInvokeExpr)
            transform(invocationBox, ((JVirtualInvokeExpr) invocationBox.getValue()));
        else if (invocationBox.getValue() instanceof JStaticInvokeExpr)
            transform(invocationBox, ((JStaticInvokeExpr) invocationBox.getValue()));
        else if (invocationBox.getValue() instanceof JSpecialInvokeExpr)
            transform(invocationBox, ((JSpecialInvokeExpr) invocationBox.getValue()), transforms);
        else
            throw new RuntimeException(String.format("Unsupported invocation type %s.",
                    invocationBox.getValue().getClass().getName()));
    }

    protected void transform(ValueBox invocationBox, JVirtualInvokeExpr invocation) {
        Value virtualInstanceReference = invocation.getBase();

        // AugmentedString.decorate(Object)
        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(AugmentedString.class.getName())
                .getMethod("decorate", Lists.newArrayList(Scene.v().getObjectType()))
                .makeRef();
        InvokeExpr newInvocation = new JStaticInvokeExpr(newMethodRef, Lists.newArrayList(virtualInstanceReference));

        invocationBox.setValue(newInvocation);
    }

    protected void transform(ValueBox invocationBox, JStaticInvokeExpr invocation) {
        // Integer.toString(int) -> AugmentedString.decorate(int)
        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(AugmentedString.class.getName())
                .getMethod("decorate", invocation.getArgs().stream().map(Value::getType).collect(Collectors.toList()))
                .makeRef();
        InvokeExpr newInvocation = new JStaticInvokeExpr(newMethodRef, invocation.getArgs());

        invocationBox.setValue(newInvocation);
    }

    protected void transform(ValueBox invocationBox, JSpecialInvokeExpr invocation,
                             CompositeExpressionTransformer transforms) {
        throw new RuntimeException("Special invocation transform is not supported.");
    }
}
