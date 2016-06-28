package org.brandonhaynes.pipegen.optimization.transforms;

public interface CompositeExpressionTransformer extends ExpressionTransformer {
    void add(ExpressionTransformer transform);
}

