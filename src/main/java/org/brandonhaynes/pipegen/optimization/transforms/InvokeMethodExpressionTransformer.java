package org.brandonhaynes.pipegen.optimization.transforms;

import com.google.common.collect.Lists;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JStaticInvokeExpr;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InvokeMethodExpressionTransformer implements ExpressionTransformer {
    private final Pattern methodNamePattern;
    private final Class<?> targetClass;
    private final Class<?> replacementClass;
    private final boolean removeOnApplication;
    private final boolean addThisAsParameter;

    public InvokeMethodExpressionTransformer(Class<?> targetClass, Class<?> replacementClass,
            String methodName, boolean removeOnApplication) {
        this(targetClass, replacementClass, Pattern.compile(methodName), removeOnApplication, false);
    }

    public InvokeMethodExpressionTransformer(Class<?> targetClass, Class<?> replacementClass, String methodName,
                                             boolean removeOnApplication, boolean addThisAsParameter) {
        this(targetClass, replacementClass, Pattern.compile(methodName), removeOnApplication, addThisAsParameter);
    }

    public InvokeMethodExpressionTransformer(Class<?> targetClass, Class<?> replacementClass,
                                             Pattern methodNamePattern, boolean removeOnApplication) {
        this(targetClass, replacementClass, methodNamePattern, removeOnApplication, false);
    }

    public InvokeMethodExpressionTransformer(Class<?> targetClass, Class<?> replacementClass,
            Pattern methodNamePattern, boolean removeOnApplication, boolean addThisAsParameter) {
        this.targetClass = targetClass;
        this.replacementClass = replacementClass;
        this.methodNamePattern = methodNamePattern;
        this.removeOnApplication = removeOnApplication;
        this.addThisAsParameter = addThisAsParameter;
    }

    protected Class<?> getTargetClass() { return targetClass; }
    protected Class<?> getReplacementClass() { return replacementClass; }

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

        if(removeOnApplication)
            output.remove(node);
    }

    protected void transform(Stmt statement, CompositeExpressionTransformer transforms) {
        ValueBox invocationBox = statement.getInvokeExprBox();

        if (invocationBox.getValue() instanceof VirtualInvokeExpr)
            transform(invocationBox, (VirtualInvokeExpr) invocationBox.getValue());
        else if (invocationBox.getValue() instanceof StaticInvokeExpr)
            transform(invocationBox, (StaticInvokeExpr) invocationBox.getValue());
        else if (invocationBox.getValue() instanceof SpecialInvokeExpr)
            transform(invocationBox, (SpecialInvokeExpr) invocationBox.getValue(), transforms);
        else if (invocationBox.getValue() instanceof InterfaceInvokeExpr)
            transform(invocationBox, (InterfaceInvokeExpr) invocationBox.getValue());
        else
            throw new RuntimeException(String.format("Unsupported invocation type %s.",
                    invocationBox.getValue().getClass().getName()));
    }

    protected void transform(ValueBox invocationBox, VirtualInvokeExpr invocation) {
        Value virtualInstanceReference = invocation.getBase();

        // AugmentedString.decorate(Object)
        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(replacementClass.getName())
                .getMethod("decorate", Lists.newArrayList(Scene.v().getObjectType()))
                .makeRef();
        InvokeExpr newInvocation = new JStaticInvokeExpr(newMethodRef, Lists.newArrayList(virtualInstanceReference));

        invocationBox.setValue(newInvocation);
    }

    protected void transform(ValueBox invocationBox, StaticInvokeExpr invocation) {
        // Integer.toString(int) -> AugmentedString.decorate(int)
        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(replacementClass.getName())
                .getMethod("decorate", invocation.getArgs().stream().map(Value::getType).collect(Collectors.toList()))
                .makeRef();
        InvokeExpr newInvocation = new JStaticInvokeExpr(newMethodRef, invocation.getArgs());

        invocationBox.setValue(newInvocation);
    }

    protected void transform(ValueBox invocationBox, InterfaceInvokeExpr invocation) {
        // Interface.toString(int) -> RecordSet.decorate(int)
        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(replacementClass.getName())
                .getMethod("decorate", getArgumentTypes(invocation))
                .makeRef();
        InvokeExpr newInvocation = new JStaticInvokeExpr(newMethodRef, getArguments(invocation));

        invocationBox.setValue(newInvocation);
    }

    protected void transform(ValueBox invocationBox, SpecialInvokeExpr invocation,
                             CompositeExpressionTransformer transforms) {
        throw new RuntimeException("Special invocation transform is not supported.");
    }

    private List<Value> getArguments(InvokeExpr invocation) {
        List<Value> arguments = addThisAsParameter && invocation instanceof InstanceInvokeExpr
                ? Lists.newArrayList(((InstanceInvokeExpr)invocation).getBase())
                : Lists.newArrayList();
        arguments.addAll(invocation.getArgs().stream().collect(Collectors.toList()));
        return arguments;
    }

    private List<Type> getArgumentTypes(InvokeExpr invocation) {
        List<Type> arguments = addThisAsParameter && invocation instanceof InstanceInvokeExpr
                ? Lists.newArrayList(Scene.v().getType(targetClass.getName()))
                //? Lists.newArrayList(((InstanceInvokeExpr)invocation).getBase().getType())
                : Lists.newArrayList();
        arguments.addAll(invocation.getArgs().stream().map(Value::getType).collect(Collectors.toList()));
        return arguments;
    }
}
