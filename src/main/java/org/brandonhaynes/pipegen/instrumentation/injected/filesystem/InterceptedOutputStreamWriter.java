package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class InterceptedOutputStreamWriter extends OutputStreamWriter {
    public static OutputStreamWriter intercept(OutputStream outputStream) throws IOException {
		return RuntimeConfiguration.getInstance().isOptimized() &&
               outputStream instanceof OptimizedInterceptedFileOutputStream
				? new OptimizedInterceptedOutputStreamWriter((OptimizedInterceptedFileOutputStream)outputStream)
				: new InterceptedOutputStreamWriter(outputStream);
	}

    public static OutputStreamWriter intercept(OutputStream outputStream, Charset charset) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                outputStream instanceof OptimizedInterceptedFileOutputStream
                ? new OptimizedInterceptedOutputStreamWriter((OptimizedInterceptedFileOutputStream)outputStream, charset)
                : new InterceptedOutputStreamWriter(outputStream, charset);
    }

    public static OutputStreamWriter intercept(OutputStream outputStream, CharsetEncoder encoder) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                outputStream instanceof OptimizedInterceptedFileOutputStream
                ? new OptimizedInterceptedOutputStreamWriter((OptimizedInterceptedFileOutputStream)outputStream, encoder)
                : new InterceptedOutputStreamWriter(outputStream, encoder);
    }

    public static OutputStreamWriter intercept(OutputStream outputStream, String charsetName) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                outputStream instanceof OptimizedInterceptedFileOutputStream
                ? new OptimizedInterceptedOutputStreamWriter((OptimizedInterceptedFileOutputStream)outputStream, charsetName)
                : new InterceptedOutputStreamWriter(outputStream, charsetName);
    }

    protected InterceptedOutputStreamWriter(OutputStream outputStream) {
        super(outputStream);
        if(outputStream instanceof OptimizedInterceptedFileOutputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized writer");
    }

    private InterceptedOutputStreamWriter(OutputStream outputStream, Charset charset) {
        super(outputStream, charset);
        if(outputStream instanceof OptimizedInterceptedFileOutputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized writer");
    }

    private InterceptedOutputStreamWriter(OutputStream outputStream, CharsetEncoder encoder) {
        super(outputStream, encoder);
        if(outputStream instanceof OptimizedInterceptedFileOutputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized writer");
    }

    private InterceptedOutputStreamWriter(OutputStream outputStream, String charsetName)
            throws UnsupportedEncodingException {
        super(outputStream, charsetName);
        if(outputStream instanceof OptimizedInterceptedFileOutputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized writer");
    }
}