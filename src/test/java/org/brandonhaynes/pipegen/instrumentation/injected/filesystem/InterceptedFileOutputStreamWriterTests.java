package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import io.netty.buffer.ArrowBuf;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.support.VectorFactories;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.junit.Test;

import java.nio.ByteBuffer;

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
        ArrowBuf arrow;
        int expectedSize;

        writer.write(new AugmentedString(1));
        writer.write(new AugmentedString(','));
        writer.write(new AugmentedString(1.5));
        writer.write(new AugmentedString(','));
        writer.write(new AugmentedString("foo"));
        writer.write(new AugmentedString('\n'));

        writer.flush();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(buffer.getInt() == 4); // # buffers

        expectedSize = buffer.getInt(); // Buffer 1 size
        assert(expectedSize == 4);
        assert(readIntoArrowBuffer(buffer, expectedSize).getInt(0) == 1);

        expectedSize = buffer.getInt(); // Buffer 2 size
        assert(expectedSize == 8);
        assert(readIntoArrowBuffer(buffer, expectedSize).getDouble(0) == 1.5);

        expectedSize = buffer.getInt(); // Buffer 3 size (varchar offsets)
        assert(expectedSize == 8);
        arrow = readIntoArrowBuffer(buffer, expectedSize);
        assert(arrow.getInt(0) == 0);
        assert(arrow.getInt(4) == 3);

        expectedSize = buffer.getInt(); // Buffer 4 size (varchar values)
        assert(expectedSize == 3);
        assert(getString(readIntoArrowBuffer(buffer, expectedSize), 0, expectedSize).equals("foo"));

        assert(!buffer.hasRemaining());
    }

    private static ArrowBuf readIntoArrowBuffer(ByteBuffer buffer, int size) {
        ArrowBuf arrowBuffer = VectorFactories.createBuffer(1024);
        byte[] bytes = new byte[size];

        buffer.get(bytes);
        arrowBuffer.writeBytes(bytes);
        arrowBuffer.getBytes(0, bytes, 0, size);
        return arrowBuffer;
    }

    private static String getString(ArrowBuf buffer, int startIndex, int endIndex) {
        byte[] bytes = new byte[endIndex - startIndex];
        buffer.getBytes(startIndex, bytes, 0, endIndex - startIndex);
        return new String(bytes);
    }
}
