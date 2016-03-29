package org.brandonhaynes.pipegen.runtime.directory;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerDirectoryEntry {
    private static AtomicInteger nextId = new AtomicInteger(0);

    public enum Direction {
        IMPORT,
        EXPORT
    }

    private final int entryId;
    private final String systemName;
    private final Direction direction;
    private final String hostname;
    private final int port;

    public WorkerDirectoryEntry(int entryId, String systemName, Direction direction, String hostname, int port) {
        this.entryId = entryId;
        this.systemName = systemName;
        this.direction = direction;
        this.hostname = hostname;
        this.port = port;
    }

    public WorkerDirectoryEntry(String systemName, Direction direction, String hostname, int port) {
        this(nextId.getAndIncrement(), systemName, direction, hostname, port);
    }

    WorkerDirectoryEntry(Iterator<String> values) {
        this(Integer.parseInt(values.next()), values.next(), Direction.valueOf(values.next()),
             values.next(), Integer.parseInt(values.next()));
    }

    WorkerDirectoryEntry(String[] values) {
        this(Integer.parseInt(values[0]), values[1], Direction.valueOf(values[2]),
                values[3], Integer.parseInt(values[4]));
    }

    public String getSystemName() { return systemName; }

    public Direction getDirection() { return direction; }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getEntryId() { return entryId; }
}
