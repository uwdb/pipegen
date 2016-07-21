package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InterceptedInputStreamReader extends InputStreamReader {
    public static InputStreamReader intercept(InputStream inputStream) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                inputStream instanceof OptimizedInterceptedFileInputStream
                ? new OptimizedInterceptedInputStreamReader((OptimizedInterceptedFileInputStream)inputStream)
                : new InterceptedInputStreamReader(inputStream);
    }

    public InterceptedInputStreamReader(InputStream inputStream) {
        super(inputStream);
        if(inputStream instanceof OptimizedInterceptedFileInputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized reader");
    }
}