package org.brandonhaynes.pipegen.configuration.tasks;

import org.brandonhaynes.pipegen.configuration.Script;

public interface ImportTask extends Task {
    Script getImportScript();
}
