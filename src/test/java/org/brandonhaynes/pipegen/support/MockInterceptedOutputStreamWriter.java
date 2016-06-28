package org.brandonhaynes.pipegen.support;

import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedOutputStreamWriter;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class MockInterceptedOutputStreamWriter extends InterceptedOutputStreamWriter {
    public MockInterceptedOutputStreamWriter(OutputStream outputStream) throws IOException {
        super(outputStream);
    }

    @Override
    public void write(String s) throws IOException {
        assert(s instanceof AugmentedString);
    }

    @Override
    public void write(String s, int i, int i1) throws IOException {
        assert(s instanceof AugmentedString);
    }

    @Override
    public Writer append(CharSequence charSequence) throws IOException {
        assert(charSequence instanceof AugmentedString);
        return this;
    }

    @Override
    public Writer append(CharSequence charSequence, int i, int i1) throws IOException {
        assert(charSequence instanceof AugmentedString);
        return this;
    }
}
