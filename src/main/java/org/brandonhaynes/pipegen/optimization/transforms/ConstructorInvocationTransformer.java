package org.brandonhaynes.pipegen.optimization.transforms;

import soot.SootMethodRef;
import soot.Value;
import soot.ValueBox;
import soot.jimple.SpecialInvokeExpr;

import java.util.stream.Collectors;

public class ConstructorInvocationTransformer extends InvokeMethodExpressionTransformer {
    public ConstructorInvocationTransformer(Class<?> clazz, Class<?> replacementClass, boolean removeOnApplication) {
        super(clazz, replacementClass, "<init>", removeOnApplication);
    }

    @Override
    protected void transform(ValueBox invocationBox, SpecialInvokeExpr invocation,
                             CompositeExpressionTransformer transforms) {
        // StringBuilder.<init>(...) -> new AugmentedStringBuilder.<init>(...)
        SootMethodRef newMethodRef = soot.Scene.v()
                .getSootClass(getReplacementClass().getName())
                .getMethod("<init>", invocation.getArgs().stream().map(Value::getType).collect(Collectors.toList()))
                .makeRef();
        invocation.setMethodRef(newMethodRef);

        // Add a one-time transform to handle instantiation, which is assumed to immediately follow this statement
        transforms.add(new OneTimeTransform(new InstantiationTransformer(getTargetClass(), getReplacementClass())));
    }
}
