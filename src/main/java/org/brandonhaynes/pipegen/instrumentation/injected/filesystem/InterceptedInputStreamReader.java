package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class InterceptedInputStreamReader extends InputStreamReader {
    public static InputStreamReader intercept(InputStream inputStream) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                inputStream instanceof OptimizedInterceptedFileInputStream
                ? new OptimizedInterceptedInputStreamReader((OptimizedInterceptedFileInputStream)inputStream)
                : new InterceptedInputStreamReader(inputStream);
    }

    public static InputStreamReader intercept(InputStream inputStream, String charsetName) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                inputStream instanceof OptimizedInterceptedFileInputStream
                ? new OptimizedInterceptedInputStreamReader((OptimizedInterceptedFileInputStream)inputStream, charsetName)
                : new InterceptedInputStreamReader(inputStream, charsetName);
    }

    public static InputStreamReader intercept(InputStream inputStream, Charset charset) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                inputStream instanceof OptimizedInterceptedFileInputStream
                ? new OptimizedInterceptedInputStreamReader((OptimizedInterceptedFileInputStream)inputStream, charset)
                : new InterceptedInputStreamReader(inputStream, charset);
    }

    public static InputStreamReader intercept(InputStream inputStream, CharsetDecoder decoder) throws IOException {
        return RuntimeConfiguration.getInstance().isOptimized() &&
                inputStream instanceof OptimizedInterceptedFileInputStream
                ? new OptimizedInterceptedInputStreamReader((OptimizedInterceptedFileInputStream)inputStream, decoder)
                : new InterceptedInputStreamReader(inputStream, decoder);
    }

    public InterceptedInputStreamReader(InputStream inputStream) {
        super(inputStream);
        if(inputStream instanceof OptimizedInterceptedFileInputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized reader");
    }

    public InterceptedInputStreamReader(InputStream inputStream, String charsetName) throws UnsupportedEncodingException {
        super(inputStream, charsetName);
        if(inputStream instanceof OptimizedInterceptedFileInputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized reader");
    }

    public InterceptedInputStreamReader(InputStream inputStream, Charset charset) throws UnsupportedEncodingException {
        super(inputStream, charset);
        if(inputStream instanceof OptimizedInterceptedFileInputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized reader");
    }

    public InterceptedInputStreamReader(InputStream inputStream, CharsetDecoder decoder)
            throws UnsupportedEncodingException {
        super(inputStream, decoder);
        if(inputStream instanceof OptimizedInterceptedFileInputStream)
            throw new RuntimeException("Data pipe failure: attempt to use optimized stream with unoptimized reader");
    }
}