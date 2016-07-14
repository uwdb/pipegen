package org.brandonhaynes.pipegen.configuration.tasks;

import org.brandonhaynes.pipegen.configuration.Script;

public interface ExportTask extends Task {
    Script getExportScript();
}
