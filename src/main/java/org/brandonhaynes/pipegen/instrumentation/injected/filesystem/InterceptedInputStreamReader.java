package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import java.io.InputStreamReader;

public class InterceptedInputStreamReader extends InputStreamReader {
    private final InterceptedFileInputStream inputStream;

    public InterceptedInputStreamReader(InterceptedFileInputStream inputStream) {
        super(inputStream);
        this.inputStream = inputStream;
    }

    public InterceptedFileInputStream getInterceptedStream() { return inputStream; }
}