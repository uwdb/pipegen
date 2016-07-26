package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;
import org.brandonhaynes.pipegen.utilities.ThreadUtilities;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;

//TODO injected classes should be moved into top-level namespace
public class InterceptedFileOutputStream extends FileOutputStream {
	private static FileDescriptor nullDescriptor = new FileDescriptor();

	public static FileOutputStream intercept(String filename) throws IOException {
		if(!RuntimeConfiguration.getInstance().getFilenamePattern(Direction.EXPORT).matcher(filename).matches())
            return new FileOutputStream(filename);
        else if(RuntimeConfiguration.getInstance().isInOptimizationMode())
            return new OptimizedInterceptedFileOutputStream(filename);
        else
            return new InterceptedFileOutputStream(filename);
	}

    public static FileOutputStream intercept(File file) throws IOException {
        if(!RuntimeConfiguration.getInstance().getFilenamePattern(Direction.EXPORT).matcher(file.getName()).matches())
            return new FileOutputStream(file);
        else if(RuntimeConfiguration.getInstance().isInOptimizationMode())
            return new OptimizedInterceptedFileOutputStream(file);
        else
            return new InterceptedFileOutputStream(file);
    }

	private final Socket socket;
	private final WorkerDirectoryEntry entry;
    protected final String filename;
	protected final OutputStream stream;

    public InterceptedFileOutputStream(File file) throws IOException {
        this(file.toString());
    }

    public InterceptedFileOutputStream(String filename) throws IOException {
        this(filename, false);
    }

	protected InterceptedFileOutputStream(String filename, boolean deferMetadata) throws IOException {
		super(nullDescriptor);
		this.filename = filename;
		this.entry = new WorkerDirectoryClient(InterceptUtilities.getSystemName(filename, Direction.EXPORT)).registerExport();
		this.socket = new Socket(entry.getHostname(), entry.getPort());
		this.stream = this.socket.getOutputStream();
        if(!deferMetadata)
            new InterceptMetadata(filename, Lists.newArrayList()).write(this.stream);
	}

    InterceptedFileOutputStream(OutputStream stream) throws IOException {
        this(stream, false);
    }

    protected InterceptedFileOutputStream(OutputStream stream, boolean deferMetadata) throws IOException {
        super(nullDescriptor);
        this.filename = null;
        this.entry = null;
        this.socket = null;
        this.stream = stream;
        if(!deferMetadata)
            new InterceptMetadata(null, Lists.newArrayList()).write(this.stream);
    }

	@Override
	public void write(@Nonnull byte[] b) throws IOException {
        stream.write(b);
	}

	@Override
	public void write(@Nonnull byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
        stream.write(b);
	}

	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	@Override
	public void	close() throws IOException {
		flush();
		super.close();
		stream.close();
        if(socket != null) {
            socket.close();
            ThreadUtilities.UncheckedSleep(RuntimeConfiguration.getInstance().getIoTimeout());
        }
    }

	@Override
	protected void finalize() throws IOException {
		super.finalize();
		close();
	}

	@Override
    @Nonnull
    public FileChannel getChannel() {
		return super.getChannel();
	}


	//@Override
	//public FileDescriptor getFD() {
	//  throw new RuntimeException("PipeGen does not support direct access to file descriptor.");
	//}
}