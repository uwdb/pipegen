package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class InterceptedOutputStreamWriter extends OutputStreamWriter {
    public static OutputStreamWriter intercept(OutputStream outputStream) throws IOException {
		return RuntimeConfiguration.getInstance().isOptimized() &&
               outputStream instanceof OptimizedInterceptedFileOutputStream
				? new OptimizedInterceptedOutputStreamWriter((OptimizedInterceptedFileOutputStream)outputStream)
				: new InterceptedOutputStreamWriter(outputStream);
	}

    protected InterceptedOutputStreamWriter(OutputStream outputStream) {
        super(outputStream);
        if(outputStream instanceof OptimizedInterceptedFileOutputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized writer");
    }
}