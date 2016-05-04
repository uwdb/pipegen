package org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_0_2_0;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class SeekableInputStream extends InputStream
        implements org.apache.hadoop.fs.Seekable, org.apache.hadoop.fs.PositionedReadable {
    private final PushbackInputStream stream;
    private long position = 0;

    SeekableInputStream(InputStream stream, int size) {
        this.stream = new PushbackInputStream(stream, size);
    }

    @Override
    public int read() throws IOException {
        position++;
        return stream.read();
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
    public synchronized void mark(int index) {
        stream.mark(index);
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
    public void seek(long offset) throws IOException {
        position += offset;
        if(offset > 0)
            while(offset > 0)
                offset =- stream.skip(offset);
        else if(offset < 0)
            stream.unread((int)-offset);
    }

    @Override
    public long getPos() throws IOException {
        return position;
    }

    @Override
    public boolean seekToNewSource(long l) throws IOException {
        return false;
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {
        throw new IOException("Not supported");
    }

    @Override
    public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
        throw new IOException("Not supported");
    }

    @Override
    public void readFully(long position, byte[] buffer) throws IOException {
        throw new IOException("Not supported");
    }
}
