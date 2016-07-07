package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class InterceptedBufferedReader extends BufferedReader {
    private final InterceptedFileInputStream inputStream;

    public InterceptedBufferedReader(Reader reader, int i) {
        super(reader, i);
        inputStream = reader instanceof InterceptedInputStreamReader
            ? ((InterceptedInputStreamReader)reader).getInterceptedStream()
            : null;
    }

    public InterceptedBufferedReader(Reader reader) {
        super(reader);
        inputStream = reader instanceof InterceptedInputStreamReader
                ? ((InterceptedInputStreamReader)reader).getInterceptedStream()
                : null;
    }

    @Override
    public int read() throws IOException {
        return super.read();
    }

    @Override
    public int read(@Nonnull char[] chars, int i, int i1) throws IOException {
        return super.read(chars, i, i1);
    }

    @Override
    public String readLine() throws IOException {
        return inputStream != null
                ? inputStream.readLine()
                : super.readLine();
    }

    @Override
    public long skip(long l) throws IOException {
        return super.skip(l);
    }

    @Override
    public boolean ready() throws IOException {
        return super.ready();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int i) throws IOException {
        throw new UnsupportedOperationException("Mark not supported.");
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
