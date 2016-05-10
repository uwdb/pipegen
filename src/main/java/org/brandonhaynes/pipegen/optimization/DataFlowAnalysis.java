package org.brandonhaynes.pipegen.optimization;

import java.util.*;
import java.util.logging.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.brandonhaynes.pipegen.optimization.sinks.SinkExpression;
import org.brandonhaynes.pipegen.optimization.transforms.ExpressionTransformer;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

public class DataFlowAnalysis extends BackwardFlowAnalysis<Unit, Set<Unit>> {
    private static final Logger log = Logger.getLogger(DataFlowAnalysis.class.getName());

    private final Map<Unit, Set<Set<Unit>>> taintedUnits;
    private final SinkExpression sinkExpression;
    private final ExpressionTransformer transformExpression;

    public DataFlowAnalysis(UnitGraph graph, SinkExpression sinkExpression, ExpressionTransformer transformExpression)
    {
        super(graph);
        this.taintedUnits = Maps.newHashMap();
        this.sinkExpression = sinkExpression;
        this.transformExpression = transformExpression;
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
        //log.info("Copying source to destination");
        destination.clear();
        destination.addAll(source);
    }

    @Override
    protected void merge(Set<Unit> left, Set<Unit> right, Set<Unit> out) {
        //log.info("Merging sets");
        out.clear();
        out.addAll(left);
        out.addAll(right);
    }

    @Override
    protected void flowThrough(Set<Unit> input, Unit node, Set<Unit> output) {
        log.info(String.format("Flow: %s -> (%s) -> %s", input, node, output));
        output.clear();
        output.addAll(input);

        if(sinkExpression.isApplicable(input, node, output))
            taintNode(node, input, output);
        if(transformExpression.isApplicable(input, node, output))
            transformExpression.transform(input, node, output);
    }

    private void taintNode(Unit node, Set<Unit> input, Set<Unit> output) {
        log.info("Tainting");
        taintedUnits.putIfAbsent(node, Sets.newHashSet());
        taintedUnits.get(node).add(input);
        output.add(node);
    }
}