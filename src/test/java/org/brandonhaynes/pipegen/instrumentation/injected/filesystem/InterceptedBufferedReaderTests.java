package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

public class InterceptedBufferedReaderTests {
    @Test
    public void testReadLine() throws Exception {
        AugmentedString aline;
        String line;

        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        OptimizedInterceptedFileOutputStream outStream = new OptimizedInterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        BufferedReader inStream =
                new InterceptedBufferedReader(
                        new InterceptedInputStreamReader(
                            new OptimizedInterceptedFileInputStream(
                                new ByteArrayInputStream(stream.toByteArray()))));

        line = inStream.readLine();
        assert(line instanceof AugmentedString);

        aline = (AugmentedString)line;
        assert(aline.getState()[0].equals(1));
        assert(aline.getState()[1].equals(1.5));
        assert(aline.getState()[2].toString().equals("foo"));

        line = inStream.readLine();
        assert(line instanceof AugmentedString);

        aline = (AugmentedString)line;
        assert(aline.getState()[0].equals(2));
        assert(aline.getState()[1].equals(2.5));
        assert(aline.getState()[2].toString().equals("bar"));

        assert(inStream.readLine() == null);
    }
}
