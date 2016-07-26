package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class OptimizedInterceptedBufferedOutputStream extends BufferedOutputStream {
	private final OptimizedInterceptedFileOutputStream outputStream;

    OptimizedInterceptedBufferedOutputStream(OptimizedInterceptedFileOutputStream outputStream) {
        super(outputStream);
		this.outputStream = outputStream;
	}

    OptimizedInterceptedBufferedOutputStream(OptimizedInterceptedFileOutputStream outputStream, int size) {
        super(outputStream, size);
        this.outputStream = outputStream;
    }

    public void write(AugmentedString value) throws IOException {
        outputStream.write(value);
    }

    @Override
    public synchronized void write(int i) throws IOException {
        outputStream.write(new AugmentedString(i));
    }

    @Override
    public void write(@Nonnull byte[] bytes) throws IOException {
        outputStream.write(new AugmentedString(bytes));
    }

    @Override
    public synchronized void write(@Nonnull byte[] bytes, int i, int i1) throws IOException {
        if(i == 0 && i1 == bytes.length)
            write(new AugmentedString(bytes));
        else
            write(new AugmentedString(Arrays.copyOfRange(bytes, i, i1)));
    }

    @Override
    public synchronized void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}