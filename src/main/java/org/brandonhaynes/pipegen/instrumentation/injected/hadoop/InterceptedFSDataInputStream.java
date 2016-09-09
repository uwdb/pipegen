package org.brandonhaynes.pipegen.instrumentation.injected.hadoop;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.ByteBufferPool;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.EnumSet;

public class InterceptedFSDataInputStream extends FSDataInputStream {
    public static FSDataInputStream intercept(FileSystem fs, Path path) throws IOException {
        if(!RuntimeConfiguration.getInstance().getFilenamePattern(Direction.IMPORT).matcher(path.toString()).matches())
            return fs.open(path);
        //else if(RuntimeConfiguration.getInstance().isOptimized())
        //    return new OptimizedInterceptedFileInputStream(filename);
        else
            return new InterceptedFSDataInputStream(path);
    }

    private final Path path;
    private final ServerSocket serverSocket;
    private final Socket socket;
    private final WorkerDirectoryEntry entry;
    protected final InputStream stream;
    protected final InterceptMetadata metadata;

    InterceptedFSDataInputStream(Path path) throws IOException {
        super(new BufferedFSInputStream(null, 1));

        this.path = path;
        this.serverSocket = new ServerSocket(0);
        this.entry = new WorkerDirectoryClient(InterceptUtilities
                .getSystemName(path.toString(), Direction.IMPORT))
                .registerImport(serverSocket.getInetAddress().getHostName(), serverSocket.getLocalPort());
        this.socket = this.serverSocket.accept();
        this.stream = this.socket.getInputStream();
        this.metadata = InterceptMetadata.read(this.stream);

        // Reset stream set in the super constructor
        this.in = stream;
    }

    @Override
    public synchronized void seek(long desired) throws IOException {
        if(desired != 0)
            throw new UnsupportedOperationException("seek:" + desired);
    }

    @Override
    public long getPos() throws IOException {
        return 0;
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        //assert(position)
        return stream.read(buffer, offset, length);
    }

    @Override
    public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
        //assert(position)
        stream.read(buffer, offset, length);
    }

    @Override
    public void readFully(long position, byte[] buffer) throws IOException {
        stream.read(buffer);
    }

    @Override
    public boolean seekToNewSource(long targetPos) throws IOException {
        throw new UnsupportedOperationException("seekto" + targetPos);
    }

    @Override
    public InputStream getWrappedStream() {
        return this.stream;
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        return stream.read(buf.array());
    }

    @Override
    public FileDescriptor getFileDescriptor() throws IOException {
        return new FileDescriptor();
    }

    @Override
    public void setReadahead(Long readahead) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDropBehind(Boolean dropBehind) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuffer read(ByteBufferPool bufferPool, int maxLength, EnumSet<ReadOption> opts) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseBuffer(ByteBuffer buffer) {
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public long skip(long l) throws IOException {
        return stream.skip(l);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
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
}
