package org.brandonhaynes.pipegen.optimization.sinks;

import com.google.common.collect.Sets;
import org.brandonhaynes.pipegen.optimization.MethodAnalysis;
import soot.*;
import soot.jimple.InvokeStmt;
import soot.toolkits.graph.UnitGraph;

import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

//import soot.baf.VirtualInvokeInst;

public class InvokeMethodSinkExpression implements SinkExpression {
    private final Pattern methodNamePattern;
    private final SootClass clazz;

    public InvokeMethodSinkExpression(Class<?> clazz, String methodName) {
        this(new SootClass(clazz.getName()), Pattern.compile(methodName));
    }

    public InvokeMethodSinkExpression(SootMethod method) {
        this(method.getDeclaringClass(), Pattern.compile(method.getName()));
    }

    public InvokeMethodSinkExpression(SootClass clazz, Pattern methodNamePattern) {
        this.clazz = clazz;
        this.methodNamePattern = methodNamePattern;
    }

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return (node instanceof InvokeStmt && isApplicable((InvokeStmt) node));
    }

    private boolean isApplicable(InvokeStmt node) {
        SootMethodRef reference = node.getInvokeExpr().getMethodRef();
        SootMethod method = node.getInvokeExpr().getMethod();
        return clazz.getName().equals(reference.declaringClass().getName()) &&
               methodNamePattern.matcher(method.getName()).matches();
    }

    @Override
    public void propagateTaint(UnitGraph graph, Set<Unit> input, Unit node, Set<Unit> output,
                               Set<Value> taintedValues, Queue<MethodAnalysis> methods) {
        assert(isApplicable(input, node, output));

        if(node instanceof InvokeStmt)
            taintedValues.addAll(getTaintedValues((InvokeStmt)node));
    }

    private Set<Value> getTaintedValues(InvokeStmt node) {
        return Sets.newHashSet(node.getInvokeExpr().getArgs());
    }
}
