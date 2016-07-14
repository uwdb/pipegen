package org.brandonhaynes.pipegen.runtime.directory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class WorkerDirectoryServer {
    private static final Logger log = Logger.getLogger(WorkerDirectoryServer.class.getName());

    private final HttpServer server;

    public static void main(String[] args) throws IOException {
        if(args.length != 1 || (!args[0].equals("production") && !args[0].equals("verification")))
            System.out.println(String.format("%s: [production|verification]\n", WorkerDirectoryServer.class.getName()));
        else if(args[0].equals("production"))
            start(new ProductionWorkerDirectory(), RuntimeConfiguration.getInstance().getWorkerDirectoryUri().getPort());
        else
            start(new VerificationWorkerDirectory(), RuntimeConfiguration.getInstance().getWorkerDirectoryUri().getPort());
    }

    private WorkerDirectoryServer(HttpServer server, Path logPropertiesFile) throws IOException {
        this(server);
        try (InputStream inputStream = new FileInputStream(logPropertiesFile.toString())) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
    }

    private WorkerDirectoryServer(HttpServer server) {
        this.server = server;
    }

    public static WorkerDirectoryServer startIfNotStarted(WorkerDirectory directory, int port) throws IOException {
        return startIfNotStarted(directory, port, null);
    }

    public static WorkerDirectoryServer startIfNotStarted(WorkerDirectory directory, int port, Path logPropertiesFile)
            throws IOException {
        try {
            return start(directory, port, logPropertiesFile);
        } catch(BindException e) {
            log.info(String.format("Could not bind to port %d; server already started.", port));
            return null;
        }
    }

    public static WorkerDirectoryServer start(WorkerDirectory directory, int port) throws IOException {
        return start(directory, port, null);
    }

    public static WorkerDirectoryServer start(WorkerDirectory directory, int port, Path logPropertiesFile)
            throws IOException {
        log.info(String.format("Starting worker directory server on port %d", port));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 100);

        server.createContext(WorkerDirectoryClient.IMPORT_PATH, (exchange) -> onImport(directory, exchange));
        server.createContext(WorkerDirectoryClient.EXPORT_PATH, (exchange) -> onExport(directory, exchange));
        server.setExecutor(null);
        server.start();

        return logPropertiesFile != null
                ? new WorkerDirectoryServer(server, logPropertiesFile)
                : new WorkerDirectoryServer(server);
    }

    public void stop(int delay) {
        log.info("Stopping worker directory server");
        this.server.stop(delay);
    }

    public void stop() { stop(false); }
    public void stop(boolean force) { stop(force ? 0 : 30); }

    private static void onImport(WorkerDirectory directory, HttpExchange exchange)
            throws IOException {
        log.info(String.format("Begin import: %s", exchange.getRequestURI()));
        Map<String, String> tokens = parseQuerystring(exchange.getRequestURI());

        WorkerDirectoryEntry entry = directory.add(
                tokens.get("system"),
                WorkerDirectoryEntry.Direction.IMPORT,
                tokens.get("hostname"),
                Integer.parseInt(tokens.get("port")));

        log.info(String.format("End import (%d): %s", entry.getEntryId(), exchange.getRequestURI()));
        sendResponse(exchange, Integer.toString(entry.getEntryId()));
    }

    private static void onExport(WorkerDirectory directory, HttpExchange exchange)
            throws IOException {
        new ExportResponseHandler(directory, exchange);
    }

    private static void sendResponse(HttpExchange exchange, String body) throws IOException {
        exchange.sendResponseHeaders(200, body.length());
        exchange.getResponseBody().write(body.getBytes());
        exchange.getResponseBody().close();
    }

    private static Map<String, String> parseQuerystring(URI uri) {
        String query = uri.getQuery();
        return Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);
    }

    private static class ExportResponseHandler implements Runnable {
        private final HttpExchange exchange;
        private final WorkerDirectory directory;

        ExportResponseHandler(WorkerDirectory directory, HttpExchange exchange) {
            this.exchange = exchange;
            this.directory = directory;
            new Thread(this).start();
        }

        public void run() {
            log.info(String.format("Begin export: %s", exchange.getRequestURI()));
            Map<String, String> tokens = parseQuerystring(exchange.getRequestURI());
            WorkerDirectoryEntry entry = directory.pop(tokens.get("system"));
            String body = Joiner.on(",").join(entry.getEntryId(), entry.getSystemName(), entry.getDirection(),
                    entry.getHostname(), entry.getPort());

            log.info(String.format("End export (%d): %s", entry.getEntryId(), exchange.getRequestURI()));

            try {
                sendResponse(exchange, body);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
