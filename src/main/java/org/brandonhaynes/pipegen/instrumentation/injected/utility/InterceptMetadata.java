package org.brandonhaynes.pipegen.instrumentation.injected.utility;

import java.io.*;
import java.nio.file.Paths;

public class InterceptMetadata implements Serializable {
    private static final long serialVersionUID = 1;

    public String filename;

    public InterceptMetadata(String filename) {
        this.filename = Paths.get(System.getProperty("user.dir")).resolve(filename).toString();
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
}
