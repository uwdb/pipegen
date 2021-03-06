package org.brandonhaynes.pipegen.optimization.sinks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.toolkits.graph.UnitGraph;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ParameterSinkExpression implements SinkExpression {
    ParameterSinkExpression() {}

    @Override
    public boolean isApplicable(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                                Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        return !input.isEmpty() &&
               node instanceof IdentityStmt &&
               ((IdentityStmt)node).getRightOp() instanceof ParameterRef;
    }

    @Override
    public void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                               Set<Value> taintedValues, Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed) {
        assert(isApplicable(graph, input, node, output, taintedValues, queue, processed));

        //TODO isApplicable should have access to tainted values...
        if(isTainted(taintedValues, node))
            queue.addAll(getCallers(graph.getBody().getMethod(),
                         ((IdentityStmt) node).getRightOp()));
    }

    private static List<MethodAnalysis> getCallers(SootMethod callee, Value value) {
        List<MethodAnalysis> methods = Lists.newArrayList();
        Scene.v().getCallGraph().edgesInto(callee).forEachRemaining(e ->
                methods.add(new MethodAnalysis(e.src(), callee, Sets.newHashSet(value))));
        return methods;
    }

    private static boolean isTainted(Set<Value> taintedValues, Unit node) {
        return node instanceof IdentityStmt && isTainted(taintedValues, ((IdentityStmt)node));
    }

    private static boolean isTainted(Set<Value> taintedValues, IdentityStmt node) {
        return taintedValues.contains(node.getLeftOp());
    }
}
