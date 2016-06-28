package org.brandonhaynes.pipegen.optimization.transforms;

import soot.Unit;

import java.util.Set;

public interface ExpressionTransformer {
    boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output);
    void transform(Set<Unit> input, Unit node, Set<Unit> output, CompositeExpressionTransformer transforms);
}

