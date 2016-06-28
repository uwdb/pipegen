package org.brandonhaynes.pipegen.optimization;

import com.google.common.collect.Sets;
import soot.SootMethod;
import soot.Value;

import java.util.Set;

public class MethodAnalysis {
    private final SootMethod caller, callee;
    private final Set<Value> taintedParameters;

    public MethodAnalysis(SootMethod caller) {
        this(caller, null, Sets.newHashSet());
    }

    public MethodAnalysis(SootMethod caller, SootMethod callee, Set<Value> taintedParameters) {
        this.caller = caller;
        this.callee = callee;
        this.taintedParameters = taintedParameters;
    }

    public SootMethod getCaller() { return caller; }
    public SootMethod getCallee() { return callee; }
    public Set<Value> getTaintedParameters() { return taintedParameters; }
}
