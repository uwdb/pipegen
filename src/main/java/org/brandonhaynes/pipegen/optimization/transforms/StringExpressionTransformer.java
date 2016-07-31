package org.brandonhaynes.pipegen.optimization.transforms;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedResultSet;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedStringBuffer;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedStringBuilder;
import soot.Unit;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Set;

public class StringExpressionTransformer implements CompositeExpressionTransformer {
    private final Collection<ExpressionTransformer> statements = Lists.newArrayList(
            new InvokeMethodExpressionTransformer(Integer.class, AugmentedString.class, "toString", false),
            new InvokeMethodExpressionTransformer(Double.class, AugmentedString.class, "toString", false),
            new InvokeMethodExpressionTransformer(Float.class, AugmentedString.class, "toString", false),
            new InvokeMethodExpressionTransformer(Object.class, AugmentedString.class, "toString", false),
            new InvokeMethodExpressionTransformer(ResultSet.class, AugmentedResultSet.class, "getString", false, true),

            new ConstructorInvocationTransformer(StringBuilder.class, AugmentedStringBuilder.class, false),
            new ConstructorInvocationTransformer(StringBuffer.class, AugmentedStringBuffer.class, false),
            new ConstructorInvocationTransformer(ResultSet.class, AugmentedResultSet.class, false));

    public StringExpressionTransformer() {}

    @Override
    public void add(ExpressionTransformer transform) {
        statements.add(transform);
    }

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return statements.stream().anyMatch(s -> s.isApplicable(input, node, output));
    }

    @Override
    public void transform(Set<Unit> input, Unit node, Set<Unit> output, CompositeExpressionTransformer transforms) {
        statements.stream()
                  .filter(s -> s.isApplicable(input, node, output))
                  .findFirst()
                  .ifPresent(s -> s.transform(input, node, output, transforms));
    }
}
