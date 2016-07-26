package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class InterceptedBufferedOutputStream extends BufferedOutputStream {
    public static BufferedOutputStream intercept(OutputStream outputStream) throws IOException {
		return RuntimeConfiguration.getInstance().isOptimized() &&
               outputStream instanceof OptimizedInterceptedFileOutputStream
				? new OptimizedInterceptedBufferedOutputStream((OptimizedInterceptedFileOutputStream)outputStream)
				: new InterceptedBufferedOutputStream(outputStream);
	}

    public static BufferedOutputStream intercept(OutputStream outputStream, int size) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                outputStream instanceof OptimizedInterceptedFileOutputStream
                ? new OptimizedInterceptedBufferedOutputStream((OptimizedInterceptedFileOutputStream)outputStream, size)
                : new InterceptedBufferedOutputStream(outputStream, size);
    }

    protected InterceptedBufferedOutputStream(OutputStream outputStream) {
        super(outputStream);
        if(outputStream instanceof OptimizedInterceptedFileOutputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized writer");
    }

    protected InterceptedBufferedOutputStream(OutputStream outputStream, int size) {
        super(outputStream);
        if(outputStream instanceof OptimizedInterceptedFileOutputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized writer");
    }
}