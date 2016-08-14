package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class InterceptedFileChannel extends FileChannel {
    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        return 0;
    }

    @Override
    public long read(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        return 0;
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        return 0;
    }

    @Override
    public long write(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        return 0;
    }

    @Override
    public long position() throws IOException {
        return 0;
    }

    @Override
    public FileChannel position(long l) throws IOException {
        return this;
    }

    @Override
    public long size() throws IOException {
        return 0;
    }

    @Override
    public FileChannel truncate(long l) throws IOException {
        return this;
    }

    @Override
    public void force(boolean b) throws IOException {

    }

    @Override
    public long transferTo(long l, long l1, WritableByteChannel writableByteChannel) throws IOException {
        return 0;
    }

    @Override
    public long transferFrom(ReadableByteChannel readableByteChannel, long l, long l1) throws IOException {
        return 0;
    }

    @Override
    public int read(ByteBuffer byteBuffer, long l) throws IOException {
        return 0;
    }

    @Override
    public int write(ByteBuffer byteBuffer, long l) throws IOException {
        return 0;
    }

    @Override
    public MappedByteBuffer map(MapMode mapMode, long l, long l1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock lock(long l, long l1, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock tryLock(long l, long l1, boolean b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void implCloseChannel() throws IOException {

    }
}
