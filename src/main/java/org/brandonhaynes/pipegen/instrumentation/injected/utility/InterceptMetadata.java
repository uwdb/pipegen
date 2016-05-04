package org.brandonhaynes.pipegen.instrumentation.injected.utility;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class InterceptMetadata implements Serializable {
    private static final long serialVersionUID = 1;

    public String filename;

    public InterceptMetadata(String filename) {
        this.filename = Paths.get(System.getProperty("user.dir")).resolve(resolveFilename(filename)).toString();
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
