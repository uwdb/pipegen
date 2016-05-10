package org.brandonhaynes.pipegen.optimization.transforms;

import com.google.common.collect.Lists;
import soot.Unit;

import java.util.Collection;
import java.util.Set;

public class StringExpressionTransformer implements ExpressionTransformer {
    private static final Collection<ExpressionTransformer> statements = Lists.newArrayList(
            new InvokeMethodExpressionTransformer(Integer.class, "toString"));

    public static ExpressionTransformer getAll() { return new StringExpressionTransformer(); }

    private StringExpressionTransformer() {}

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return statements.stream().anyMatch(s -> s.isApplicable(input, node, output));
    }

    @Override
    public void transform(Set<Unit> input, Unit node, Set<Unit> output) {
        statements.stream()
                  .filter(s -> s.isApplicable(input, node, output))
                  .findFirst().ifPresent(s -> s.transform(input, node, output));
    }
}
