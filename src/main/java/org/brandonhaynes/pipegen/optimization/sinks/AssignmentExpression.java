package org.brandonhaynes.pipegen.optimization.sinks;

import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import org.brandonhaynes.pipegen.utilities.ClassUtilities;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.toolkits.graph.UnitGraph;

import java.util.Queue;
import java.util.Set;

public class AssignmentExpression implements SinkExpression {
    AssignmentExpression() {}

    @Override
    public boolean isApplicable(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                                Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        return !input.isEmpty() &&
               node instanceof AssignStmt &&
               ((AssignStmt)node).getRightOp() instanceof InvokeExpr;
    }

    @Override
    public void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                               Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        assert(isApplicable(graph, input, node, output, taintedValues, queue, processed));

        SootMethod method = ((InvokeExpr)((AssignStmt)node).getRightOp()).getMethod();

        if(isTainted(taintedValues, node))
            if(!ClassUtilities.getOriginalUnaugmentedClasses(Scene.v()).contains(method.getDeclaringClass()))
                queue.add(new MethodAnalysis(method, true));
    }

    private static boolean isTainted(Set<Value> taintedValues, Unit node) {
        return node instanceof AssignStmt && isTainted(taintedValues, ((AssignStmt)node));
    }

    private static boolean isTainted(Set<Value> taintedValues, AssignStmt node) {
        return taintedValues.contains(node.getLeftOp());
    }
}
