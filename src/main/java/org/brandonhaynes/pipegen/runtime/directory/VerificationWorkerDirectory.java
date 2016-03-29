package org.brandonhaynes.pipegen.runtime.directory;

import com.google.common.collect.Queues;

import java.util.concurrent.BlockingDeque;

public class VerificationWorkerDirectory implements WorkerDirectory {
    private final BlockingDeque<WorkerDirectoryEntry> entries = Queues.newLinkedBlockingDeque();

    public WorkerDirectoryEntry add(String systemName, WorkerDirectoryEntry.Direction direction,
                                    String hostname, int port) {
        WorkerDirectoryEntry entry = new WorkerDirectoryEntry(systemName, direction, hostname, port);
        add(entry);
        return entry;
    }

    public void add(WorkerDirectoryEntry entry) {
        try {
            entries.putFirst(entry);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public WorkerDirectoryEntry pop(String systemName) {
        try {
            return entries.takeFirst();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
