package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import sun.nio.cs.StreamDecoder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

public class OptimizedInterceptedInputStreamReader extends InputStreamReader {
    private final OptimizedInterceptedFileInputStream inputStream;
    private final StreamDecoder decoder;

    public OptimizedInterceptedInputStreamReader(OptimizedInterceptedFileInputStream inputStream) {
        super(inputStream);
        this.inputStream = inputStream;

        try {
            this.decoder = StreamDecoder.forInputStreamReader(inputStream, this, (String) null);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    OptimizedInterceptedFileInputStream getInputStream() { return inputStream; }

    @Override
    public int read(@Nonnull CharBuffer charBuffer) throws IOException {
        return decoder.read(charBuffer);
    }

    @Override
    public int read() throws IOException {
        return decoder.read();
    }

    @Override
    public int read(@Nonnull char[] chars) throws IOException {
        return decoder.read(chars);
    }

    @Override
    public int read(@Nonnull char[] chars, int i, int i1) throws IOException {
        return decoder.read(chars, i, i1);
    }

    @Override
    public long skip(long l) throws IOException {
        return decoder.skip(l);
    }

    @Override
    public boolean ready() throws IOException {
        return decoder.ready();
    }

    @Override
    public boolean markSupported() {
        return decoder.markSupported();
    }

    @Override
    public void close() throws IOException {
        super.close();
        inputStream.close();
        decoder.close();
    }
}