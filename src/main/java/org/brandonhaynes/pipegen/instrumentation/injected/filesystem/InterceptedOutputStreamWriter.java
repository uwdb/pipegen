package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class InterceptedOutputStreamWriter extends OutputStreamWriter {
    public static OutputStreamWriter intercept(OutputStream outputStream) throws IOException {
		return outputStream instanceof InterceptedFileOutputStream
				? new InterceptedOutputStreamWriter((InterceptedFileOutputStream)outputStream)
				: new OutputStreamWriter(outputStream);
	}

	public static Collection<Class> getDependencies() {
		return new ArrayList<Class>() {{
            add(InterceptedOutputStreamWriter.class);
            add(AugmentedString.class);
		}};
	}

	private final InterceptedFileOutputStream outputStream;

    protected InterceptedOutputStreamWriter(InterceptedFileOutputStream outputStream) {
        super(outputStream);
		this.outputStream = outputStream;
	}

    protected InterceptedOutputStreamWriter(OutputStream outputStream) {
        super(outputStream);
        this.outputStream = null;
    }

    @Override
    public void write(int i) throws IOException {
        outputStream.write(new AugmentedString(i));
    }

    @Override
    public void write(@Nonnull char[] chars) throws IOException {
        outputStream.write(new AugmentedString(chars));
    }

    @Override
    public void write(@Nonnull char[] chars, int i, int i1) throws IOException {
        outputStream.write(new AugmentedString(Arrays.copyOfRange(chars, i, i1)));
    }

    @Override
    public void write(@Nullable String s) throws IOException {
        outputStream.write(new AugmentedString(s));
    }

    @Override
    public void write(@Nullable String s, int i, int i1) throws IOException {
        if(s != null)
            outputStream.write(new AugmentedString(s.substring(i, i1)));
    }

    @Override
    public Writer append(CharSequence charSequence) throws IOException {
        outputStream.write(new AugmentedString(charSequence));
        return this;
    }

    @Override
    public Writer append(CharSequence charSequence, int i, int i1) throws IOException {
        outputStream.write(new AugmentedString(charSequence.subSequence(i, i1)));
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