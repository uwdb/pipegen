package org.brandonhaynes.pipegen.mutation.rules;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;

public class ImportExportRule extends CompositeRule {
    public ImportExportRule(CompileTimeConfiguration configuration) {
        super(new SaveTraceRule(configuration),
              //new ImportRule(configuration.importTask),
              new ExportRule(configuration.exportTask));
    }
}
