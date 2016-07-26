package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import sun.nio.cs.StreamEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

public class OptimizedInterceptedOutputStreamWriter extends OutputStreamWriter {
	private final OptimizedInterceptedFileOutputStream outputStream;
    private final StreamEncoder encoder;

    OptimizedInterceptedOutputStreamWriter(OptimizedInterceptedFileOutputStream outputStream) {
        super(outputStream);
		this.outputStream = outputStream;

        try {
            this.encoder = StreamEncoder.forOutputStreamWriter(outputStream, this, (String)null);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
	}

    OptimizedInterceptedOutputStreamWriter(OptimizedInterceptedFileOutputStream outputStream, Charset charset) {
        super(outputStream, charset);
        this.outputStream = outputStream;
        this.encoder = StreamEncoder.forOutputStreamWriter(outputStream, this, charset);
    }

    OptimizedInterceptedOutputStreamWriter(OptimizedInterceptedFileOutputStream outputStream, CharsetEncoder encoder) {
        super(outputStream, encoder);
        this.outputStream = outputStream;
        this.encoder = StreamEncoder.forOutputStreamWriter(outputStream, this, encoder);
    }

    OptimizedInterceptedOutputStreamWriter(OptimizedInterceptedFileOutputStream outputStream, String charsetName)
            throws UnsupportedEncodingException {
        super(outputStream, charsetName);
        this.outputStream = outputStream;
        this.encoder = StreamEncoder.forOutputStreamWriter(outputStream, this, charsetName);
    }

    public void write(AugmentedString s) throws IOException {
        outputStream.write(s);
    }

    @Override
    public void write(int i) throws IOException {
        write(new AugmentedString(i));
    }

    @Override
    public void write(@Nonnull char[] chars) throws IOException {
        write(new AugmentedString(chars));
    }

    @Override
    public void write(@Nonnull char[] chars, int i, int i1) throws IOException {
        write(new AugmentedString(Arrays.copyOfRange(chars, i, i1)));
    }

    @Override
    public void write(@Nullable String s) throws IOException {
        write(new AugmentedString(s));
    }

    @Override
    public void write(@Nullable String s, int i, int i1) throws IOException {
        if(s != null)
            write(new AugmentedString(s.substring(i, i1)));
    }

    @Override
    public Writer append(CharSequence charSequence) throws IOException {
        write(new AugmentedString(charSequence));
        return this;
    }

    @Override
    public Writer append(CharSequence charSequence, int i, int i1) throws IOException {
        write(new AugmentedString(charSequence.subSequence(i, i1)));
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}