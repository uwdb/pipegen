package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;

public class OptimizedInterceptedBufferedReader extends BufferedReader {
    private final OptimizedInterceptedInputStreamReader reader;

    public OptimizedInterceptedBufferedReader(OptimizedInterceptedInputStreamReader reader) {
        super(reader);
        this.reader = reader;
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public int read(@Nonnull char[] chars, int i, int i1) throws IOException {
        return reader.read(chars, i, i1);
    }

    @Override
    public String readLine() throws IOException {
        return reader.getInputStream().readLine();
    }

    @Override
    public long skip(long l) throws IOException {
        return reader.skip(l);
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }

    @Override
    public void mark(int i) throws IOException {
        reader.mark(i);
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }

    @Override
    public void close() throws IOException {
        super.close();
        reader.close();
    }
}
