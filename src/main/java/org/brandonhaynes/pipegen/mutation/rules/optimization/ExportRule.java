package org.brandonhaynes.pipegen.mutation.rules.optimization;

import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.mutation.rules.CompositeRule;
import org.brandonhaynes.pipegen.mutation.rules.SaveTraceRule;

public class ExportRule extends CompositeRule {
    public ExportRule(OptimizationTask task) {
        super(new SaveTraceRule(task.getConfiguration()),
              new OutputStreamWriterRule(task),
              new BufferedWriterRule(task),
              new ExportSinkRule(task));
    }
}
