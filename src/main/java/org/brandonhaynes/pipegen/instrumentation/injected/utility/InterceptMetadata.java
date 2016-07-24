package org.brandonhaynes.pipegen.instrumentation.injected.utility;

import org.apache.zookeeper.server.ByteBufferInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;

import static org.brandonhaynes.pipegen.utilities.PathUtilities.resolveFilename;

public class InterceptMetadata implements Serializable {
    private static final long serialVersionUID = 1;

    public final String filename;
    public final Class<?>[] vectorClasses;
    public final Throwable exception;

    public InterceptMetadata(String filename, List<Class<?>> vectorClasses) {
        this(filename, vectorClasses.toArray(new Class<?>[vectorClasses.size()]));
    }

    public InterceptMetadata(String filename, Class<?>[] vectorClasses) {
        this(filename, vectorClasses, null);
    }

    public InterceptMetadata(String filename, Class<?>[] vectorClasses, Throwable e) {
        this.filename = filename != null
                ? Paths.get(System.getProperty("user.dir")).resolve(resolveFilename(filename)).toString()
                : null;
        this.vectorClasses = vectorClasses;
        this.exception = e;
    }

    public void write(OutputStream stream) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(stream); {
            output.writeObject(this);
        }
    }

    public static InterceptMetadata read(InputStream stream) throws IOException {
        ObjectInputStream input = new ObjectInputStream(stream); {
            try {
                return throwOnFailure((InterceptMetadata) input.readObject());

            } catch(ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }

    public static InterceptMetadata read(ByteBuffer buffer) throws IOException {
        return read(new ByteBufferInputStream(buffer));
    }

    private static InterceptMetadata throwOnFailure(InterceptMetadata metadata) throws IOException {
        if(metadata.exception instanceof IOException)
            throw ((IOException)metadata.exception);
        else if(metadata.exception != null)
            throw new IOException(metadata.exception);
        else
            return metadata;
    }
}
