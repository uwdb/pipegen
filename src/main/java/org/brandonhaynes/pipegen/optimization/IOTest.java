package org.brandonhaynes.pipegen.optimization;

import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;

import java.io.*;

// Move these into formal tests
public class IOTest extends Main {
    public String foo = "asdf";

    public String bar() {
        return foo;
    }

    public void boxedWriterTest1() throws IOException {
        FileOutputStream stream = new InterceptedFileOutputStream("/tmp/foo");
        OutputStreamWriter writer = new OutputStreamWriter(stream);

        Integer boxed = 5;
        //String converted = Integer.toString(5);
        String converted = boxed.toString();

        //new OutputStreamWriter(stream).write((String)AugmentedString.decorate((Object)5).toString());
        writer.write(converted);
    }
}
