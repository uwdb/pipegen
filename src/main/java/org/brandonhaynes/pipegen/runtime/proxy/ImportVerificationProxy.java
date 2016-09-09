package org.brandonhaynes.pipegen.runtime.proxy;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.brandonhaynes.pipegen.utilities.PathUtilities.resolveFilename;

public class ImportVerificationProxy implements VerificationProxy, Runnable {
    private static final Logger log = Logger.getLogger(ImportVerificationProxy.class.getName());

    private final Thread thread = new Thread(this);
    private final WorkerDirectoryClient client = new WorkerDirectoryClient("*");
    private final Path basePath;
    private volatile boolean isRunning = true;

    public ImportVerificationProxy(Path basePath) {
        this.basePath = basePath;
    }

    public void start() {
        log.info("Starting import verification proxy");
        this.thread.start();
    }

    public void stop() {
        log.info("Stopping import verification proxy");
        this.isRunning = false;
        this.thread.interrupt();
    }

    public void run() {
        while (isRunning)
            try {
                export();
            } catch(SocketTimeoutException e) {
                // TODO abort hanging registerExport rather than waiting on a socket timeout
                if(this.isRunning)
                    throw new RuntimeException(e);
            } catch(IOException e) {
                if(isRunning) {
                    log.severe(e.toString());
                    throw new RuntimeException(e);
                }
            }
    }

    private synchronized void export() throws IOException {
        log.info("Beginning verification");
        WorkerDirectoryEntry entry = client.registerExport();
        log.info("Obtained directory entry " + entry.getEntryId());

        try(Socket socket = new Socket(entry.getHostname(), entry.getPort())) {
            try(OutputStream stream = socket.getOutputStream()) {
                log.info(String.format("Sending %s to importer", entry.getSystemName()));

                try {
                    try (InputStream input = new FileInputStream(Paths.get(basePath.toString())
                                                     .resolve(resolveFilename(entry.getSystemName())).toFile())) {
                        int bytesRead;
                        byte[] buffer = new byte[4096];

                        new InterceptMetadata(entry.getSystemName(), Lists.newArrayList()).write(stream);

                        while ((bytesRead = input.read(buffer)) != -1)
                            stream.write(buffer, 0, bytesRead);
                    }
                } catch(FileNotFoundException e) {
                    new InterceptMetadata(entry.getSystemName(), new Class[0], e).write(stream);
                }
            }
        }
    }
}
