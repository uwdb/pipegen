package org.brandonhaynes.pipegen.runtime.directory;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.brandonhaynes.pipegen.configuration.Direction;

import java.util.Map;
import java.util.concurrent.BlockingDeque;

public class ProductionWorkerDirectory implements WorkerDirectory {
    private final Map<String, BlockingDeque<WorkerDirectoryEntry>> entries = Maps.newConcurrentMap();

    public WorkerDirectoryEntry add(String systemName, Direction direction,
                                    String hostname, int port) {
        WorkerDirectoryEntry entry = new WorkerDirectoryEntry(systemName, direction, hostname, port);
        add(entry);
        return entry;
    }

    public void add(WorkerDirectoryEntry importer) {
        try {
            getQueue(importer.getSystemName()).putFirst(importer);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public WorkerDirectoryEntry pop(String systemName) {
        try {
            return getQueue(systemName).takeFirst();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private BlockingDeque<WorkerDirectoryEntry> getQueue(String systemName) {
        if(!entries.containsKey(systemName))
            entries.put(systemName, Queues.newLinkedBlockingDeque());
        return entries.get(systemName);
    }
}
