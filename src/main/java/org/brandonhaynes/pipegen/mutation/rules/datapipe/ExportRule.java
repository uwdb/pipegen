package org.brandonhaynes.pipegen.mutation.rules.datapipe;

import org.brandonhaynes.pipegen.configuration.tasks.ExportTask;
import org.brandonhaynes.pipegen.mutation.rules.CompositeRule;
import org.brandonhaynes.pipegen.mutation.rules.SaveTraceRule;

public class ExportRule extends CompositeRule {
    public ExportRule(ExportTask task) {
        super(new SaveTraceRule(task.getConfiguration()),
              new HadoopFileSystemCreateRule(task),
              new FileOutputStreamRule(task));
    }
}
