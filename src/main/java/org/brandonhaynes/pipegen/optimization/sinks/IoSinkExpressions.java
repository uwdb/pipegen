package org.brandonhaynes.pipegen.optimization.sinks;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import soot.Scene;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.UnitGraph;

import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;

public class IoSinkExpressions implements SinkExpression {
    private final Collection<SinkExpression> statements;

    public IoSinkExpressions(Scene scene) {
        statements = Lists.newArrayList(
                new InvokeMethodSinkExpression(scene, OutputStreamWriter.class, "write"),
                new InvokeMethodSinkExpression(scene, Appendable.class, "append"),
                new ParameterSinkExpression(),
                new ArrayIndexExpression(),
                new AssignmentExpression(),
                new ReturnStatementSink());
                //new InvokeMethodSinkExpression(InterceptedFileOutputStream.class, "write"));
    }

    public void add(SinkExpression expression) {
        statements.add(expression);
    }

    @Override
    public boolean isApplicable(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                                Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        return statements.stream().anyMatch(s -> s.isApplicable(graph, input, node, output,
                                                                taintedValues, queue, processed));
    }

    @Override
    public void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                               Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        statements.stream()
                  .filter(s -> s.isApplicable(graph, input, node, output, taintedValues, queue, processed))
                  .forEach(s -> s.propagateTaint(graph, input, node, output, taintedValues, queue, processed));
    }
}
