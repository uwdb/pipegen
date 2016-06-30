package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;

import javax.annotation.Nonnull;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;

public class InterceptedFileInputStream extends FileInputStream {
	private static FileDescriptor nullDescriptor = new FileDescriptor();

	public static FileInputStream intercept(String filename) throws IOException {
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

	//public InterceptedFileInputStream(File file) { super(nullDescriptor); }

	//public InterceptedFileInputStream(FileDescriptor descriptor) { super(nullDescriptor);	}

	public InterceptedFileInputStream(String filename) throws IOException {
        super(nullDescriptor);
        this.filename = filename;
//		try {
			this.serverSocket = new ServerSocket(0);
			this.entry = new WorkerDirectoryClient(InterceptUtilities.getSystemName(filename)).registerImport(
					serverSocket.getInetAddress().getHostName(), serverSocket.getLocalPort());
			this.socket = this.serverSocket.accept();
			this.stream = this.socket.getInputStream();
			//this.stream = WorkerDirectoryClient.connectImport(entry);
			InterceptMetadata.read(this.stream);
//		} catch(Exception e) {
//			throw new IOException(e);
//		}
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
		return super.getChannel();
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
	public int read(@Nonnull byte[] b, int off, int len) throws IOException {
		return stream.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return stream.skip(n);
	}
}