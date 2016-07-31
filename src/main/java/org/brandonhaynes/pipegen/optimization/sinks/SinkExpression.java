package org.brandonhaynes.pipegen.optimization.sinks;

import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.UnitGraph;

import java.util.Queue;
import java.util.Set;

public interface SinkExpression {
    boolean isApplicable(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output, Set<Value> taintedValues,
                         Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed);
    void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                        Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed);
}

