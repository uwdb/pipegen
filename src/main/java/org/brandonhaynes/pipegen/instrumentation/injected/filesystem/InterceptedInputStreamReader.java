package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import java.io.InputStreamReader;

public class InterceptedInputStreamReader extends InputStreamReader {
    private final OptimizedInterceptedFileInputStream inputStream;

    public InterceptedInputStreamReader(OptimizedInterceptedFileInputStream inputStream) {
        super(inputStream);
        this.inputStream = inputStream;
    }

    public OptimizedInterceptedFileInputStream getInterceptedStream() { return inputStream; }
}