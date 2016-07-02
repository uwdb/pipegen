package org.brandonhaynes.pipegen.instrumentation.injected.utility;

import org.apache.zookeeper.server.ByteBufferInputStream;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

public class InterceptMetadata implements Serializable {
    private static final long serialVersionUID = 1;

    public final String filename;
    public final Class<?>[] vectorClasses;

    public InterceptMetadata(String filename, List<Class<?>> vectorClasses) {
        this(filename, vectorClasses.toArray(new Class<?>[vectorClasses.size()]));
    }

    public InterceptMetadata(String filename, Class<?>[] vectorClasses) {
        this.filename = filename != null
                ? Paths.get(System.getProperty("user.dir")).resolve(resolveFilename(filename)).toString()
                : null;
        this.vectorClasses = vectorClasses;
    }

    public void write(OutputStream stream) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(stream); {
            output.writeObject(this);
        }
    }

    public static InterceptMetadata read(InputStream stream) throws IOException {
        ObjectInputStream input = new ObjectInputStream(stream); {
            try {
                return (InterceptMetadata) input.readObject();
            } catch(ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }

    public static InterceptMetadata read(ByteBuffer buffer) throws IOException {
        return read(new ByteBufferInputStream(buffer));
    }

    private static String resolveFilename(String filename) {
        try {
            URI uri = new URI(filename);
            if (uri.getScheme() == null || uri.getScheme().equals("file"))
                return uri.getPath();
            else
                throw new RuntimeException("Scheme not supported: " + uri.getScheme());
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
