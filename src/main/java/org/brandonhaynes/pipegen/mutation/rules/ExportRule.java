package org.brandonhaynes.pipegen.mutation.rules;

import org.brandonhaynes.pipegen.configuration.ExportTask;

public class ExportRule extends CompositeRule {
    public ExportRule(ExportTask task) {
        super(new SaveTraceRule(task.getConfiguration()), new FileOutputStreamRule(task));
    }
}
