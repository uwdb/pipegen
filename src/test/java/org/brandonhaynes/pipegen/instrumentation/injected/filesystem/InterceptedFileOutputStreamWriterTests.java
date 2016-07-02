package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import io.netty.buffer.ArrowBuf;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStreamTests.assertColumn;

public class InterceptedFileOutputStreamWriterTests {
    @Test
    public void testSingleWrite() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        InterceptedOutputStreamWriter writer = new InterceptedOutputStreamWriter(iStream);
        writer.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));

        CompositeVector vector = iStream.getVector();
        assert (vector.getVectors().size() == 3);
        assert (vector.getVectors().get(0).getClass() == IntVector.class);
        assert (vector.getVectors().get(1).getClass() == Float8Vector.class);
        assert (vector.getVectors().get(2).getClass() == VarCharVector.class);
    }

    @Test
    public void testVectorFlush() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        InterceptedOutputStreamWriter writer = new InterceptedOutputStreamWriter(iStream);
        ArrowBuf offsets;

        writer.write(new AugmentedString(1));
        writer.write(new AugmentedString(','));
        writer.write(new AugmentedString(1.5));
        writer.write(new AugmentedString(','));
        writer.write(new AugmentedString("foo"));
        writer.write(new AugmentedString('\n'));

        writer.flush();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(InterceptMetadata.read(buffer) != null);
        assert(buffer.getInt() == 4); // # buffers

        assertColumn(buffer, 4, new Integer[] {1});
        assertColumn(buffer, 8, new Double[] {1.5});
        offsets = assertColumn(buffer, 8, new Integer[] {0, 3});
        assertColumn(buffer, offsets, 3, new String[] {"foo"});

        assert(!buffer.hasRemaining());
    }
}
