package org.brandonhaynes.pipegen.mutation.rules;

import org.brandonhaynes.pipegen.configuration.OptimizationTask;

public class ExportOptimizationRule extends CompositeRule {
    public ExportOptimizationRule(OptimizationTask task) {
        super(new SaveTraceRule(task.getConfiguration()));
    }
}
