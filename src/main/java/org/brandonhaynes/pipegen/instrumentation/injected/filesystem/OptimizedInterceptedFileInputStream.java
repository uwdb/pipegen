package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import com.google.common.annotations.VisibleForTesting;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.brandonhaynes.pipegen.utilities.StreamUtilities;
import org.brandonhaynes.pipegen.utilities.StringUtilities;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.brandonhaynes.pipegen.utilities.StringUtilities.intersperse;

public class OptimizedInterceptedFileInputStream extends InterceptedFileInputStream {
    private static ByteBuffer emptyBuffer = ByteBuffer.allocate(0);

    private final CompositeVector vector;
    private final byte[] buffer = new byte[RuntimeConfiguration.getInstance().getBufferAllocationSize()];
    private ByteBuffer pendingBuffer = emptyBuffer;
    private boolean isEOFDetected = false;

    public OptimizedInterceptedFileInputStream(String filename) throws IOException {
        super(filename);
        this.vector = ColumnUtilities.createVector(metadata.vectorClasses);
        vector.allocateNew(RuntimeConfiguration.getInstance().getVarCharSize(),
                           RuntimeConfiguration.getInstance().getVectorSize());
	}

	@VisibleForTesting
	OptimizedInterceptedFileInputStream(InputStream stream) throws IOException {
		super(stream);
        this.vector = ColumnUtilities.createVector(metadata.vectorClasses);
        vector.allocateNew(RuntimeConfiguration.getInstance().getVarCharSize(),
                           RuntimeConfiguration.getInstance().getVectorSize());
	}

    @VisibleForTesting
    CompositeVector getVector() { return vector; }

	@Override
	public int read() throws IOException {
		return getBuffer().hasRemaining()
            ? getBuffer().get()
            : -1;
	}

	@Override
	public int read(@Nonnull byte[] b) throws IOException {
        return read(b, 0, b.length);
	}

	@Override
	public int read(@Nonnull byte[] bytes, int offset, int length) throws IOException {
        int copied = 0;

        while(offset > getBuffer().remaining()) {
            offset -= getBuffer().remaining();
            pendingBuffer = emptyBuffer;
        }

        getBuffer().position(offset);

        while(length > 0 && getBuffer().hasRemaining()) {
            int remaining = Math.min(getBuffer().remaining(), length);
            getBuffer().get(bytes, copied, remaining);
            length -= remaining;
            copied += remaining;
        }

        return copied;
	}

	public AugmentedString readLine() throws IOException {
        if(isEOF())
            return null;
        else if(!pendingBuffer.hasRemaining())
            return vector.getReader().read();
        else
            return new AugmentedString(StringUtilities.toString(pendingBuffer));
    }

    private boolean isEOF() throws IOException {
        return isEOFDetected ||
               (!pendingBuffer.hasRemaining() &&
                !vector.getReader().hasRemaining() &&
                (isEOFDetected = !StreamUtilities.readVectors(stream, vector, buffer)));
    }

    private ByteBuffer getBuffer() throws IOException {
        if(!isEOF() && !pendingBuffer.hasRemaining())
            pendingBuffer = ByteBuffer.wrap(intersperse(vector.getReader().read(), ',', '\n').getBytes());

        return !isEOF() ? pendingBuffer : emptyBuffer;
    }
}