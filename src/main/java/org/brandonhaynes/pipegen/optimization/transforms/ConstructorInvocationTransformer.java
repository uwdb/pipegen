package org.brandonhaynes.pipegen.optimization.transforms;

import soot.SootMethodRef;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JSpecialInvokeExpr;

import java.util.stream.Collectors;

public class ConstructorInvocationTransformer extends InvokeMethodExpressionTransformer {
    private final Class<?> replacementClass;

    public ConstructorInvocationTransformer(Class<?> clazz, Class<?> replacementClass, boolean removeOnApplication) {
        super(clazz, "<init>", removeOnApplication);
        this.replacementClass = replacementClass;
    }

    @Override
    protected void transform(ValueBox invocationBox, JSpecialInvokeExpr invocation,
                             CompositeExpressionTransformer transforms) {
        // StringBuilder.<init>(...) -> new AugmentedStringBuilder.<init>(...)
        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(replacementClass.getName())
                .getMethod("<init>", invocation.getArgs().stream().map(Value::getType).collect(Collectors.toList()))
                .makeRef();
        invocation.setMethodRef(newMethodRef);

        // Add a one-time transform to handle instantiation, which is assumed to immediately follow this statement
        transforms.add(new OneTimeTransform(new InstantiationTransformer(getTargetClass(), replacementClass)));
    }
}
