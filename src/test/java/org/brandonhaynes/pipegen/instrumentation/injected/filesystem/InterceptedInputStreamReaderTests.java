package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class InterceptedInputStreamReaderTests {
    @Test
    public void testGetter() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream = new InterceptedFileInputStream(
                                                  new ByteArrayInputStream(stream.toByteArray()));
        InterceptedInputStreamReader reader = new InterceptedInputStreamReader(inStream);

        assert(reader.getInterceptedStream() == inStream);
    }
}
