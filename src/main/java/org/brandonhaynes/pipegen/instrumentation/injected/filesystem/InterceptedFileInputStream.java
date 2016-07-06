package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import com.google.common.annotations.VisibleForTesting;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.brandonhaynes.pipegen.utilities.StreamUtilities;

import javax.annotation.Nonnull;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;

public class InterceptedFileInputStream extends FileInputStream {
	private static FileDescriptor nullDescriptor = new FileDescriptor();
    private static ByteBuffer emptyBuffer = ByteBuffer.allocate(0);

	public static FileInputStream intercept(String filename) throws IOException {
        //TODO need to check runtime configuration here to use optimized or unoptimized versions
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(filename).matches()
				? new InterceptedFileInputStream(filename)
				: new FileInputStream(filename);
	}

    public static Collection<Class> getDependencies() {
        return new ArrayList<Class>() {{
            add(InterceptedFileInputStream.class);
			add(InterceptUtilities.class);
			add(InterceptMetadata.class);
            add(RuntimeConfiguration.class);
            add(WorkerDirectoryClient.class);
			add(WorkerDirectoryEntry.class);
			add(WorkerDirectoryEntry.Direction.class);
        }};
    }

    private final String filename;
	private final ServerSocket serverSocket;
	private final Socket socket;
    private final WorkerDirectoryEntry entry;
    private final InputStream stream;
    private final InterceptMetadata metadata;
    private final CompositeVector vector;
    private final byte[] buffer = new byte[RuntimeConfiguration.getInstance().getBufferAllocationSize()];
    private ByteBuffer pendingBuffer = emptyBuffer;

	public InterceptedFileInputStream(String filename) throws IOException {
        super(nullDescriptor);
        this.filename = filename;
		this.serverSocket = new ServerSocket(0);
		this.entry = new WorkerDirectoryClient(InterceptUtilities.getSystemName(filename)).registerImport(
				serverSocket.getInetAddress().getHostName(), serverSocket.getLocalPort());
		this.socket = this.serverSocket.accept();
		this.stream = this.socket.getInputStream();
        this.metadata = InterceptMetadata.read(this.stream);
        this.vector = ColumnUtilities.createVector(metadata.vectorClasses);
        vector.allocateNew(RuntimeConfiguration.getInstance().getVarCharSize(),
                           RuntimeConfiguration.getInstance().getVectorSize());
	}

	@VisibleForTesting
	InterceptedFileInputStream(InputStream stream) throws IOException {
		super(nullDescriptor);
		this.filename = null;
		this.serverSocket = null;
		this.entry = null;
		this.socket = null;
		this.stream = stream;
        this.metadata = InterceptMetadata.read(this.stream);
        this.vector = ColumnUtilities.createVector(metadata.vectorClasses);
        vector.allocateNew(RuntimeConfiguration.getInstance().getVarCharSize(),
                           RuntimeConfiguration.getInstance().getVectorSize());
	}

    @VisibleForTesting
    CompositeVector getVector() { return vector; }



	@Override
	public int available() throws IOException {
		return stream.available();
	}

	@Override
	public void close() throws IOException {
		super.close();
        stream.close();
		socket.close();
		serverSocket.close();
	}

	@Override
	public synchronized void mark(int i) {
		stream.mark(i);
	}

	@Override
	public synchronized void reset() throws IOException {
		stream.reset();
	}

	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}

	@Override
	protected void finalize() throws IOException {
		super.finalize();
		close();
	}

	@Override
	public FileChannel getChannel() {
		return super.getChannel();
	}

	//@Override
	//public FileDescriptor getFD() {
	//	return super.getFD();
	//}


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

	@Override
	public long skip(long n) throws IOException {
		return stream.skip(n);
	}

    private ByteBuffer getBuffer() throws IOException {
        if(pendingBuffer != null && !pendingBuffer.hasRemaining()) {
            if(!vector.getReader().hasRemaining())
                if (!StreamUtilities.readVectors(stream, vector, buffer))
                    pendingBuffer = null;
            if (pendingBuffer != null)
                pendingBuffer = ByteBuffer.wrap(AugmentedString.separate(vector.getReader().read(), ',', '\n').getBytes());
        }
        return pendingBuffer != null
                ? pendingBuffer
                : emptyBuffer;
    }
}