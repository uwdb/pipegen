package org.brandonhaynes.pipegen.optimization;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.brandonhaynes.pipegen.optimization.sinks.SinkExpression;
import org.brandonhaynes.pipegen.optimization.transforms.CompositeExpressionTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

public class DataFlowAnalysis extends BackwardFlowAnalysis<Unit, Set<Unit>> {
    private static final Logger log = Logger.getLogger(DataFlowAnalysis.class.getName());

    private final Map<Unit, Set<Set<Unit>>> taintedUnits;
    private final Set<Value> taintedValues;
    private final Queue<MethodAnalysis> taintedCallers;
    private final SinkExpression sinkExpression;
    private final CompositeExpressionTransformer transformExpression;
    private final UnitGraph graph;

    public DataFlowAnalysis(Queue<MethodAnalysis> queue, MethodAnalysis current,
                            SinkExpression sinkExpression, CompositeExpressionTransformer transformExpression) {
        this(queue, current.getCaller(), current.getTaintedParameters(),
                sinkExpression, transformExpression);
    }

    public DataFlowAnalysis(Queue<MethodAnalysis> queue, SootMethod method, Set<Value> taintedValues,
                            SinkExpression sinkExpression, CompositeExpressionTransformer transformExpression) {
        this(queue, new ExceptionalUnitGraph(method.getActiveBody()), taintedValues,
             sinkExpression, transformExpression);
    }

    public DataFlowAnalysis(Queue<MethodAnalysis> queue, UnitGraph graph, Set<Value> taintedValues,
                            SinkExpression sinkExpression, CompositeExpressionTransformer transformExpression) {
        super(graph);
        this.graph = graph;
        this.taintedUnits = Maps.newHashMap();
        this.taintedCallers = queue;
        this.taintedValues = taintedValues;
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
        log.info(String.format("Flow: %s %d %s -> (%s) -> %s (%s line %d)", graph.getBody().getMethod().getName(), input.size(), input, node, output, graph.getBody().getClass().getName(), node.getJavaSourceStartLineNumber()));
        output.clear();
        output.addAll(input);

        if(sinkExpression.isApplicable(input, node, output))
            taintNode(node, input, output);
        if(transformExpression.isApplicable(input, node, output))
            transformExpression.transform(input, node, output, transformExpression);
    }

    private void taintNode(Unit node, Set<Unit> input, Set<Unit> output) {
        log.info("Tainting");

        sinkExpression.propagateTaint(graph, input, node, output, taintedValues, taintedCallers);
        taintedUnits.putIfAbsent(node, Sets.newHashSet());
        taintedUnits.get(node).add(input);

        output.add(node);
    }

    private void transformNode() {
        // perform transform
        // remove applicable values from taintedParameter list
    }
}