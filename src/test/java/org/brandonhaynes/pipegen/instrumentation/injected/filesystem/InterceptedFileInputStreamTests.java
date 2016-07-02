package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class InterceptedFileInputStreamTests {
    @Test
    public void testInference() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));

        InterceptedFileInputStream inStream = new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        CompositeVector vector = inStream.getVector();
        assert (vector.getVectors().size() == 3);
        assert (vector.getVectors().get(0).getClass() == IntVector.class);
        assert (vector.getVectors().get(1).getClass() == Float8Vector.class);
        assert (vector.getVectors().get(2).getClass() == VarCharVector.class);
    }
}
