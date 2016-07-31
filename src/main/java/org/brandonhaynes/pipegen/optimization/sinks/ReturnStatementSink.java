package org.brandonhaynes.pipegen.optimization.sinks;

import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import soot.Unit;
import soot.Value;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.UnitGraph;

import java.util.Queue;
import java.util.Set;

public class ReturnStatementSink implements SinkExpression {
    @Override
    public boolean isApplicable(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                                Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        return node instanceof ReturnStmt &&
               isApplicable(graph, input, (ReturnStmt)node, output, queue, processed);
    }

    public boolean isApplicable(UnitGraph graph, Set<Unit> input, ReturnStmt node, Set<Unit> output,
                                Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        return queue.stream().anyMatch(m -> hasTaintedCallers(graph, m)) ||
               processed.stream().anyMatch(m -> hasTaintedCallers(graph, m));
    }

    @Override
    public void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                               Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        assert(isApplicable(graph, input, node, output, taintedValues, queue, processed));

        taintedValues.add(((ReturnStmt)node).getOp());
    }

    private static boolean hasTaintedCallers(UnitGraph graph, MethodAnalysis candidate) {
        return candidate.getCaller() != null &&
               candidate.getCaller().equals(graph.getBody().getMethod()) &&
               candidate.hasTaintedCallers();
    }
}
