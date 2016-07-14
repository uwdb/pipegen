package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class InterceptedBufferedWriter extends BufferedWriter {
    public static BufferedWriter intercept(Writer writer) throws IOException {
		return RuntimeConfiguration.getInstance().isOptimized() &&
               writer instanceof InterceptedOutputStreamWriter
				? new OptimizedInterceptedBufferedWriter((InterceptedOutputStreamWriter)writer)
				: new InterceptedBufferedWriter(writer);
	}

    private InterceptedBufferedWriter(Writer writer) {
		super(writer);
		if(writer instanceof OptimizedInterceptedOutputStreamWriter)
			throw new RuntimeException(
					"Data pipe failure: attempt to use optimized writer with unoptimized buffered writer");
	}
}