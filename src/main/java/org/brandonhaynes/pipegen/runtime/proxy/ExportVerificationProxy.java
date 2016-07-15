package org.brandonhaynes.pipegen.runtime.proxy;

import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ExportVerificationProxy implements VerificationProxy, Runnable {
    private static final Logger log = Logger.getLogger(ExportVerificationProxy.class.getName());

    private final ServerSocket serverSocket;
    private final WorkerDirectoryClient client = new WorkerDirectoryClient("*", 10, 60);
    private final Path basePath;
    private volatile boolean isRunning = true;
    private volatile Thread thread;

    public ExportVerificationProxy(Path basePath) throws IOException {
        this.basePath = basePath;
        this.serverSocket = new ServerSocket(0);
    }

    public void start() {
        log.info("Starting export verification proxy");
        thread = new Thread(this);
        this.thread.start();
    }

    public void stop() {
        log.info("Stopping export verification proxy");
        this.isRunning = false;

        try {
            this.serverSocket.close();
            this.thread.interrupt();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        while (isRunning)
            try {
                verifyImport();
            } catch(SocketException e) {
                // TODO abort hanging registerImport rather than waiting on a socket timeout
                if(this.isRunning)
                    throw new RuntimeException(e);
            } catch(IOException e) {
                log.severe(e.toString());
                throw new RuntimeException(e);
            }
    }

    private synchronized void verifyImport() throws IOException {
        log.info("Beginning verification");

        WorkerDirectoryEntry importEntry = client.registerImport(serverSocket.getInetAddress().getHostName(),
                                                                 serverSocket.getLocalPort());
        log.info(String.format("Obtained directory entry %d for path %s", importEntry.getEntryId(),
                                                                          importEntry.getSystemName()));

        try(Socket socket = serverSocket.accept()) {
            try(InputStream stream = socket.getInputStream()) {
                InterceptMetadata metadata = InterceptMetadata.read(stream);
                log.info(String.format("Receiving %s from exporter", metadata.filename));

                basePath.resolve(Paths.get(metadata.filename)).getParent().toFile().mkdirs();
                try(OutputStream output = new FileOutputStream(basePath.resolve(Paths.get(metadata.filename)).toFile())) {
                    int bytesRead;
                    byte[] buffer = new byte[4096];

                    while ((bytesRead = stream.read(buffer)) != -1)
                        output.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
