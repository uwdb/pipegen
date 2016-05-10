package org.brandonhaynes.pipegen.optimization.sinks;

import soot.Unit;

import java.util.Set;

public interface SinkExpression {
    boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output);
}

