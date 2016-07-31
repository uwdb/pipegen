package org.brandonhaynes.pipegen.optimization.sinks;

import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.toolkits.graph.UnitGraph;

import java.util.Queue;
import java.util.Set;

public class ArrayIndexExpression implements SinkExpression {
    ArrayIndexExpression() {}

    @Override
    public boolean isApplicable(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                                Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        return !input.isEmpty() &&
               node instanceof AssignStmt &&
               ((AssignStmt)node).getRightOp() instanceof ArrayRef;
    }

    @Override
    public void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                               Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        assert(isApplicable(graph, input, node, output, taintedValues, queue, processed));

        if(isTainted(taintedValues, node))
            taintedValues.add(((ArrayRef)((AssignStmt)node).getRightOp()).getBase());
    }

    private static boolean isTainted(Set<Value> taintedValues, Unit node) {
        return node instanceof AssignStmt && isTainted(taintedValues, ((AssignStmt)node));
    }

    private static boolean isTainted(Set<Value> taintedValues, AssignStmt node) {
        return taintedValues.contains(node.getLeftOp());
    }
}
