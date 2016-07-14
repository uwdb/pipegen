package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

public class OptimizedInterceptedOutputStreamWriter extends OutputStreamWriter {
	private final OptimizedInterceptedFileOutputStream outputStream;

    OptimizedInterceptedOutputStreamWriter(OptimizedInterceptedFileOutputStream outputStream) {
        super(outputStream);
		this.outputStream = outputStream;
	}

    protected OptimizedInterceptedOutputStreamWriter(OutputStream outputStream) {
        super(outputStream);
        this.outputStream = null;
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