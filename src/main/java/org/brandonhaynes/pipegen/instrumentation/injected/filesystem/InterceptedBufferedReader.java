package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class InterceptedBufferedReader extends BufferedReader {
    public static BufferedReader intercept(Reader reader) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                reader instanceof OptimizedInterceptedInputStreamReader
                ? new OptimizedInterceptedBufferedReader((OptimizedInterceptedInputStreamReader)reader)
                : new InterceptedBufferedReader(reader);
    }

    public static BufferedReader intercept(Reader reader, int size) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                reader instanceof OptimizedInterceptedInputStreamReader
                ? new OptimizedInterceptedBufferedReader((OptimizedInterceptedInputStreamReader)reader, size)
                : new InterceptedBufferedReader(reader, size);
    }

    public InterceptedBufferedReader(Reader reader, int size) {
        super(reader, size);
        if(reader instanceof OptimizedInterceptedInputStreamReader)
            throw new RuntimeException(
                    "Data pipe failure: attempt to use optimized reader with unoptimized buffered reader");
    }

    public InterceptedBufferedReader(Reader reader) {
        super(reader);
        if(reader instanceof OptimizedInterceptedInputStreamReader)
            throw new RuntimeException(
                    "Data pipe failure: attempt to use optimized reader with unoptimized buffered reader");
    }
}
