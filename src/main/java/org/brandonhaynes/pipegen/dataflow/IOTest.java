package org.brandonhaynes.pipegen.dataflow;

import java.io.*;

// Move these into formal tests
public class IOTest {
    public void boxedWriterTest() throws IOException {
        FileOutputStream stream = new FileOutputStream("/tmp/foo");
        OutputStreamWriter writer = new OutputStreamWriter(stream);

        Integer boxed = 5;
        String converted = boxed.toString();

        writer.write(converted);
    }
}
