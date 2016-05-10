package org.brandonhaynes.pipegen.optimization.sinks;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;
import soot.Unit;

import java.util.Collection;
import java.util.Set;

public class IoSinkExpressions implements SinkExpression {
    private static final Collection<SinkExpression> statements = Lists.newArrayList(
            new InvokeMethodSinkExpression(InterceptedFileOutputStream.class, "write"));

    public static SinkExpression getAll() { return new IoSinkExpressions(); }

    private IoSinkExpressions() {}

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return statements.stream().anyMatch(s -> s.isApplicable(input, node, output));
    }
}
