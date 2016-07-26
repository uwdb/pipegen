package org.brandonhaynes.pipegen.mutation.rules.optimization;

import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.mutation.rules.CompositeRule;
import org.brandonhaynes.pipegen.mutation.rules.SaveTraceRule;
import org.brandonhaynes.pipegen.mutation.rules.optimization.sinks.BufferedReaderSinkRule;

public class ImportRule extends CompositeRule {
    public ImportRule(OptimizationTask task) {
        super(new SaveTraceRule(task.getConfiguration()),
              new InputStreamReaderRule(task),
              new BufferedReaderRule(task),

              new BufferedReaderSinkRule(task));
    }
}
