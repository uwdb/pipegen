package org.brandonhaynes.pipegen.optimization;

import com.google.common.collect.Sets;
import soot.SootMethod;
import soot.Value;

import java.util.Set;

public class MethodAnalysis {
    private final SootMethod caller, callee;
    private final Set<Value> taintedParameters;
    private final boolean hasTaintedCaller;

    public MethodAnalysis(SootMethod caller) {
        this(caller, null, Sets.newHashSet(), false);
    }

    public MethodAnalysis(SootMethod caller, boolean hasTaintedCallers) {
        this(caller, null, Sets.newHashSet(), hasTaintedCallers);
    }

    public MethodAnalysis(SootMethod caller, SootMethod callee,
                          Set<Value> taintedParameters) {
        this(caller, callee, taintedParameters, false);
    }

    public MethodAnalysis(SootMethod caller, SootMethod callee,
                          Set<Value> taintedParameters, boolean hasTaintedCallers) {
        this.caller = caller;
        this.callee = callee;
        this.taintedParameters = taintedParameters;
        this.hasTaintedCaller = hasTaintedCallers;
    }

    public SootMethod getCaller() { return caller; }
    public SootMethod getCallee() { return callee; }
    public Set<Value> getTaintedParameters() { return taintedParameters; }
    public boolean hasTaintedCallers() { return hasTaintedCaller; }
}
