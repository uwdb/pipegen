package org.brandonhaynes.pipegen.optimization.transforms;

import soot.Unit;

import java.util.Set;

public class OneTimeTransform implements ExpressionTransformer {
    private final ExpressionTransformer targetTransform;
    private boolean transformed = false;

    public OneTimeTransform(ExpressionTransformer targetTransform) {
        this.targetTransform = targetTransform;
    }

    public ExpressionTransformer getTargetTransform() { return targetTransform; }

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return !transformed && targetTransform.isApplicable(input, node, output);
    }

    @Override
    public void transform(Set<Unit> input, Unit node, Set<Unit> output, CompositeExpressionTransformer transforms) {
        transformed = true;
        targetTransform.transform(input, node, output, transforms);
    }
}
