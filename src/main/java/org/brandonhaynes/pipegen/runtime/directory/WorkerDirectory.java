package org.brandonhaynes.pipegen.runtime.directory;

import java.util.Collection;

public interface WorkerDirectory {
    WorkerDirectoryEntry add(String systemName, WorkerDirectoryEntry.Direction direction, String hostname, int port);
    void add(WorkerDirectoryEntry importer);
    WorkerDirectoryEntry pop(String systemName);
}
