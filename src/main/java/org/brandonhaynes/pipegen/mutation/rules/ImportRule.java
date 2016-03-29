package org.brandonhaynes.pipegen.mutation.rules;

import org.brandonhaynes.pipegen.configuration.ImportTask;

public class ImportRule extends CompositeRule {
    public ImportRule(ImportTask task) {
        super(new SaveTraceRule(task.getConfiguration()),
              new HadoopFileSystemOpenRule(task),
              new FileInputStreamRule(task));
    }
}
