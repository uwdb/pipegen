package org.brandonhaynes.pipegen.runtime.directory;

import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class WorkerDirectoryClient {
    static final String IMPORT_PATH = "/import";
    static final String EXPORT_PATH = "/export";
    private static final int DEFAULT_CONNECT_TIMEOUT = 30;
    private static final int DEFAULT_READ_TIMEOUT = 360;

    private final String systemName;
    private final int connectTimeout;
    private final int readTimeout;

    public WorkerDirectoryClient(String systemName) {
        this(systemName, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public WorkerDirectoryClient(String systemName, int connectTimeout, int readTimeout) {
        this.systemName = systemName;
        this.connectTimeout = connectTimeout * 1000;
        this.readTimeout = readTimeout * 1000;
    }

    public WorkerDirectoryEntry registerImport(String hostname, int port) throws IOException {
        try(InputStreamReader reader = new InputStreamReader(openStream(getImportUri(systemName, hostname, port)))) {
            int entryId = Integer.parseInt(read(reader, 1024));
            //int entryId = Integer.parseInt(new LineReader(reader).readLine());
            return new WorkerDirectoryEntry(entryId, systemName, Direction.IMPORT, hostname, port);
        }
    }

    public WorkerDirectoryEntry registerExport() throws IOException {
        try(InputStreamReader reader = new InputStreamReader(openStream(getExportUri(systemName)))) {
            return new WorkerDirectoryEntry(read(reader, 1024).split(","));
        }
    }

    private static URI getImportUri(String systemName, String hostname, int port) {
        return makeUri(IMPORT_PATH, "system", systemName, "hostname", hostname, "port", port); }

    private static URI getExportUri(String systemName) {
        return makeUri(EXPORT_PATH, "system", systemName); }

    private static URI makeUri(String path, Object... tokens) {
        try {
            URI uri = RuntimeConfiguration.getInstance().getWorkerDirectoryUri();
            return new URI(
                    uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath() + path,
                    join(createMap(tokens), "&", "="),
                    uri.getFragment());
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream openStream(URI uri) throws IOException {
        URLConnection connection = uri.toURL().openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.connect();
        return connection.getInputStream();
    }

    private static Map<String, String> createMap(Object... values) {
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < values.length; i += 2)
            map.put(values[i].toString(), values[i+1].toString());
        return map;
    }

    private static String join(Map<String, String> map, String delimiter, String pairDelimiter) {
        // Used in place of Guava's joiner so that we don't have to add another dependency to modified DBMS
        // Also avoid Java 8's String.join method in case we augment a Java 7 system.
        StringBuilder buffer = new StringBuilder();

        for(String key: map.keySet())
            buffer.append(key).append(pairDelimiter).append(map.get(key)).append(delimiter);

        return (buffer.length() > 0
                    ? buffer.deleteCharAt(buffer.length() - 1)
                    : buffer).toString();
    }

    private static String read(InputStreamReader reader, int size) throws IOException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[size];
        int read = reader.read(buffer, 0, size);
        builder.append(buffer, 0, read);
        return builder.toString();
    }
}
