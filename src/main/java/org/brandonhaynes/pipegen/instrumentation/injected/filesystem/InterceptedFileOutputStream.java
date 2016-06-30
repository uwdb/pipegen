package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.ValueVector;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InterceptedFileOutputStream extends FileOutputStream {
	private static FileDescriptor nullDescriptor = new FileDescriptor();

	public static FileOutputStream intercept(String filename) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(filename).matches()
				? new InterceptedFileOutputStream(filename)
				: new FileOutputStream(filename);
	}

    public static FileOutputStream intercept(File file) throws IOException {
        return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(file.getName()).matches()
                ? new InterceptedFileOutputStream(file)
                : new FileOutputStream(file);
    }

	public static Collection<Class> getDependencies() {
		return new ArrayList<Class>() {{
			add(InterceptedFileOutputStream.class);
			add(InterceptUtilities.class);
			add(InterceptMetadata.class);
			add(RuntimeConfiguration.class);
			add(WorkerDirectoryClient.class);
			add(WorkerDirectoryEntry.class);
			add(WorkerDirectoryEntry.Direction.class);
		}};
	}

	private final String filename;
	private final Socket socket;
	private final WorkerDirectoryEntry entry;
	private final OutputStream stream;
    private final BufferAllocator allocator = new RootAllocator(RuntimeConfiguration.getInstance().getBufferAllocationSize());
    private List<ValueVector> vectors;
    private boolean isInferred = false;
    private AugmentedString inferenceEvidence = AugmentedString.empty;

    public InterceptedFileOutputStream(File file) throws IOException {
        this(file.getName());
    }

	public InterceptedFileOutputStream(String filename) throws IOException {
		super(nullDescriptor);
		this.filename = filename;
		this.entry = new WorkerDirectoryClient(InterceptUtilities.getSystemName(filename)).registerExport();
		this.socket = new Socket(entry.getHostname(), entry.getPort());
		this.stream = this.socket.getOutputStream();
		new InterceptMetadata(filename).write(this.stream);
	}

    public List<ValueVector> getVectors() { return vectors; }
    public boolean getIsInferred() { return isInferred; }

	public void write(AugmentedString value) throws IOException {
        if(isInferred)
    		stream.write(value.getBytes());
        else {
            inferenceEvidence = AugmentedString.concat(inferenceEvidence, value);
            if(inferenceEvidence.containsNonNumeric("\n")) {
                vectors = ColumnUtilities.createVectors(allocator, inferenceEvidence);
                isInferred = true;
                write(inferenceEvidence);
            }
        }
	}

	@Override
	public void write(@Nonnull byte[] b) throws IOException {
        write(new AugmentedString(b));
	}

	@Override
	public void write(@Nonnull byte[] b, int off, int len) throws IOException {
        byte[] bytes = new byte[len];
        System.arraycopy(b, off, bytes, 0, len);
        write(new AugmentedString(bytes));
	}

	@Override
	public void write(int b) throws IOException {
        write(new AugmentedString(b));
	}

	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	@Override
	public void	close() throws IOException {
		super.close();
		stream.close();
		socket.close();
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