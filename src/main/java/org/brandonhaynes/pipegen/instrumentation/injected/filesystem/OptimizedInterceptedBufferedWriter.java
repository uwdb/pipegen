package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class OptimizedInterceptedBufferedWriter extends BufferedWriter {
	private final InterceptedOutputStreamWriter writer;

    OptimizedInterceptedBufferedWriter(InterceptedOutputStreamWriter writer) {
        super(writer);
		this.writer = writer;
	}

    public void write(AugmentedString s) throws IOException {
        writer.write(s);
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
    public void newLine() throws IOException {
        write(AugmentedString.newline);
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
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}