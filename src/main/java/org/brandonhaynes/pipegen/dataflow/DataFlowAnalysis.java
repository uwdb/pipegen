package org.brandonhaynes.pipegen.dataflow;
import java.util.*;
import java.util.function.BiConsumer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

public class DataFlowAnalysis extends BackwardFlowAnalysis<Unit, Set<Unit>> {

    private final Map<Unit, Set<Set<Unit>>> taintedUnits;
    private final Set<String> sinkStatements;
    private final BiConsumer<Unit, Set<Unit>> transform;

    public DataFlowAnalysis(UnitGraph graph, Set<String> sinkStatements, BiConsumer<Unit, Set<Unit>> transform)
    {
        super(graph);
        this.taintedUnits = Maps.newHashMap();
        this.sinkStatements = sinkStatements;
        this.transform = transform;
        doAnalysis();
    }

    @Override
    protected Set<Unit> entryInitialFlow() {
        return Sets.newHashSet();
    }

    @Override
    protected Set<Unit> newInitialFlow() {
        return Sets.newHashSet();
    }

    @Override
    protected void copy(Set<Unit> source, Set<Unit> destination) {
        destination.clear();
        destination.addAll(source);
    }

    @Override
    protected void merge(Set<Unit> left, Set<Unit> right, Set<Unit> out) {
        out.clear();
        out.addAll(left);
        out.addAll(right);
    }

    @Override
    protected void flowThrough(Set<Unit> input, Unit node, Set<Unit> output) {
        output.clear();
        output.addAll(input);

        if(isTaintedUnit(node))
            taintNode(node, input, output);

        System.out.println(input);
        System.out.println(node);
        System.out.println(output);
        System.out.println("------");
    }

    private void taintNode(Unit node, Set<Unit> input, Set<Unit> output) {
        taintedUnits.putIfAbsent(node, Sets.newHashSet());
        taintedUnits.get(node).add(input);
        output.add(node);
        transform.accept(node, input);
    }

    private boolean isTaintedUnit(Unit node) {
        return node instanceof JAssignStmt && isTaintedUnit((JAssignStmt) node);
    }

    private boolean isTaintedUnit(JAssignStmt node) {
        return sinkStatements.contains(node.getRightOpBox().getValue().toString());
    }
}