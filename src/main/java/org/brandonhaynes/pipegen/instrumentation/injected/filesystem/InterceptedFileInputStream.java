package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import com.google.common.annotations.VisibleForTesting;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;

public class InterceptedFileInputStream extends FileInputStream {
	private static FileDescriptor nullDescriptor = new FileDescriptor();

	public static FileInputStream intercept(File file) throws IOException {
		return intercept(file.toString());
	}

	public static FileInputStream intercept(String filename) throws IOException {
		if(!RuntimeConfiguration.getInstance().getFilenamePattern(Direction.IMPORT).matcher(filename).matches())
			return new FileInputStream(filename);
		else if(RuntimeConfiguration.getInstance().isOptimized())
			return new OptimizedInterceptedFileInputStream(filename);
		else
			return new InterceptedFileInputStream(filename);
	}

    private final String filename;
	private final ServerSocket serverSocket;
	private final Socket socket;
    private final WorkerDirectoryEntry entry;
    protected final InputStream stream;
	protected final InterceptMetadata metadata;

    public InterceptedFileInputStream(String filename) throws IOException {
        super(nullDescriptor);
        this.filename = filename;
		this.serverSocket = new ServerSocket(0);
		this.entry = new WorkerDirectoryClient(InterceptUtilities
				.getSystemName(filename, Direction.IMPORT))
                .registerImport(serverSocket.getInetAddress().getHostName(), serverSocket.getLocalPort());
		this.socket = this.serverSocket.accept();
		this.stream = this.socket.getInputStream();
		this.metadata = InterceptMetadata.read(this.stream);
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
	}

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
		return new InterceptedFileChannel();
	}

	//@Override
	//public FileDescriptor getFD() {
	//	return super.getFD();
	//}


	@Override
	public int read() throws IOException {
		return stream.read();
	}

	@Override
	public int read(@Nonnull byte[] b) throws IOException {
		return stream.read(b);
	}

	@Override
	public int read(@Nonnull byte[] bytes, int offset, int length) throws IOException {
		return stream.read(bytes, offset, length);
	}

	@Override
	public long skip(long n) throws IOException {
		return stream.skip(n);
	}
}