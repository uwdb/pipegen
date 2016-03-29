package org.brandonhaynes.pipegen.instrumentation.injected.jackson;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.OutputDecorator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class RemappingOutputDecorator extends OutputDecorator {
    @Override
    public OutputStream decorate(IOContext ioContext, OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Writer decorate(IOContext ioContext, Writer writer) throws IOException {
        return new RemappingJsonWriter(writer);
    }
}
