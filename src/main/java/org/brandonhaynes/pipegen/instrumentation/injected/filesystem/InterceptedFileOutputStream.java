package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import com.google.common.annotations.VisibleForTesting;
import io.netty.buffer.ArrowBuf;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.brandonhaynes.pipegen.utilities.CompositeVector;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;

import static org.brandonhaynes.pipegen.utilities.StreamUtilities.convertInteger;

public class InterceptedFileOutputStream extends FileOutputStream {
	private static FileDescriptor nullDescriptor = new FileDescriptor();
    private static final int VECTOR_LIMIT = 4096;
    private static final int VARCHAR_SIZE = 1024;

	public static FileOutputStream intercept(String filename) throws IOException {
        //TODO need to check runtime configuration here to use optimized or unoptimized versions
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
    private CompositeVector vector;
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
	}

    @VisibleForTesting
    InterceptedFileOutputStream(OutputStream stream) throws IOException {
        super(nullDescriptor);
        this.filename = null;
        this.entry = null;
        this.socket = null;
        this.stream = stream;
    }

    @VisibleForTesting
    CompositeVector getVector() { return vector; }
    private boolean getIsInferred() { return vector != null; }
    private boolean getIsVectorFull() { return vector.getAccessor().getValueCount() > VECTOR_LIMIT; }

	public void write(AugmentedString value) throws IOException {
        if(!getIsInferred())
            addInferenceEvidence(value);
        else if(!getIsVectorFull())
            addToVector(value);
        else
            writeVector(value);
	}

    private void addToVector(AugmentedString value) {
        vector.getMutator().set(value);
    }
    private void writeVector(AugmentedString value) throws IOException {
        ArrowBuf[] buffers = vector.getBuffers(false);

        stream.write(convertInteger(buffers.length));
        for(ArrowBuf buffer: buffers) {
            stream.write(convertInteger(buffer.readableBytes()));
            buffer.getBytes(0, stream, buffer.readableBytes());
        }

        if(value != null)
            write(value);
    }

    private void addInferenceEvidence(AugmentedString value) throws IOException  {
        inferenceEvidence = AugmentedString.concat(inferenceEvidence, value);
        if(inferenceEvidence.containsNonNumeric("\n")) {
            vector = ColumnUtilities.createVector(inferenceEvidence);
            vector.allocateNew(VARCHAR_SIZE, VECTOR_LIMIT);
            new InterceptMetadata(filename, vector.getClasses()).write(this.stream);
            write(inferenceEvidence);
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
        writeVector(null);
		stream.flush();
	}

	@Override
	public void	close() throws IOException {
        if(!getIsInferred())
            write(AugmentedString.newline);
        flush();
		super.close();
		stream.close();
        if(socket != null)
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