package org.brandonhaynes.pipegen.runtime.proxy;

import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectory;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ImportVerificationProxy implements VerificationProxy, Runnable {
    private static final Logger log = Logger.getLogger(ImportVerificationProxy.class.getName());

    private final Thread thread = new Thread(this);
    private final WorkerDirectoryClient client = new WorkerDirectoryClient("*", 10, 60);
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
                log.severe(e.toString());
                throw new RuntimeException(e);
            }
    }

    private synchronized void export() throws IOException {
        log.info("Beginning verification");
        WorkerDirectoryEntry entry = client.registerExport();
        log.info("Obtained directory entry " + entry.getEntryId());

        try(Socket socket = new Socket(entry.getHostname(), entry.getPort())) {
            try(OutputStream stream = socket.getOutputStream()) {
                new InterceptMetadata(entry.getSystemName()).write(stream);

                log.info(String.format("Sending %s to importer", entry.getSystemName()));

                try(InputStream input = new FileInputStream(Paths.get(basePath.toString()).resolve(getImportPath(entry)).toFile())) {
                //try(InputStream input = new FileInputStream(Paths.get(basePath.toString(), getImportPath(entry)).toString())) {
                    int bytesRead;
                    byte[] buffer = new byte[4096];

                    while ((bytesRead = input.read(buffer)) != -1)
                        stream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    private String getImportPath(WorkerDirectoryEntry entry) {
        try {
            URI uri = new URI(entry.getSystemName());
            if (uri.getScheme() == null || uri.getScheme().equals("file"))
                return uri.getPath();
            else
                throw new RuntimeException("Scheme not supported: " + uri.getScheme());
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
