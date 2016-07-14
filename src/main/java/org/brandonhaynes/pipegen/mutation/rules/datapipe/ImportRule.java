package org.brandonhaynes.pipegen.mutation.rules.datapipe;

import org.brandonhaynes.pipegen.configuration.tasks.ImportTask;
import org.brandonhaynes.pipegen.mutation.rules.CompositeRule;
import org.brandonhaynes.pipegen.mutation.rules.SaveTraceRule;

public class ImportRule extends CompositeRule {
    public ImportRule(ImportTask task) {
        super(new SaveTraceRule(task.getConfiguration()),
              new HadoopFileSystemOpenRule(task),
              new FileInputStreamRule(task));
    }
}
