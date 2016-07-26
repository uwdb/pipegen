package org.brandonhaynes.pipegen.mutation.rules.optimization;

import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.mutation.rules.CompositeRule;
import org.brandonhaynes.pipegen.mutation.rules.SaveTraceRule;
import org.brandonhaynes.pipegen.mutation.rules.optimization.sinks.BufferedOutputStreamSinkRule;
import org.brandonhaynes.pipegen.mutation.rules.optimization.sinks.BufferedWriterSinkRule;
import org.brandonhaynes.pipegen.mutation.rules.optimization.sinks.WriterSinkRule;

public class ExportRule extends CompositeRule {
    public ExportRule(OptimizationTask task) {
        super(new SaveTraceRule(task.getConfiguration()),
              new OutputStreamWriterRule(task),
              new BufferedWriterRule(task),
              new BufferedOutputStreamRule(task),

              new BufferedWriterSinkRule(task),
              new BufferedOutputStreamSinkRule(task),
              new WriterSinkRule(task));
    }
}
