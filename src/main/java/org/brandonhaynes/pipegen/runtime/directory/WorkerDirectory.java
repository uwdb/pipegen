package org.brandonhaynes.pipegen.runtime.directory;

import org.brandonhaynes.pipegen.configuration.Direction;

public interface WorkerDirectory {
    WorkerDirectoryEntry add(String systemName, Direction direction, String hostname, int port);
    void add(WorkerDirectoryEntry importer);
    WorkerDirectoryEntry pop(String systemName);
}
