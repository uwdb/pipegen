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

public class InterceptedFileOutputStreamTests {
    @Test
    public void testInference() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        iStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));

        CompositeVector vector = iStream.getVector();
        assert (vector.getVectors().size() == 3);
        assert (vector.getVectors().get(0).getClass() == IntVector.class);
        assert (vector.getVectors().get(1).getClass() == Float8Vector.class);
        assert (vector.getVectors().get(2).getClass() == VarCharVector.class);
    }

    @Test
    public void testInferenceMultipleWrites() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        iStream.write(new AugmentedString(1));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString(1.5));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString("foo"));
        iStream.write(new AugmentedString('\n'));

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
        ArrowBuf arrow;
        int expectedSize;

        iStream.write(new AugmentedString(1));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString(1.5));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString("foo"));
        iStream.write(new AugmentedString('\n'));

        iStream.flush();

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

    @Test
    public void testMultiRowVectorFlush() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        ArrowBuf arrow, offsets;
        int expectedSize;

        iStream.write(new AugmentedString(123456789));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString(11111111.5));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString("foo"));
        iStream.write(new AugmentedString('\n'));
        iStream.write(new AugmentedString(234567890, ',', 22222222.5, ',', "bar", "\n"));

        iStream.flush();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(buffer.getInt() == 4); // # buffers

        expectedSize = buffer.getInt(); // Buffer 1 size
        assert(expectedSize == 8);
        arrow = readIntoArrowBuffer(buffer, expectedSize);
        assert(arrow.getInt(0) == 123456789);
        assert(arrow.getInt(4) == 234567890);

        expectedSize = buffer.getInt(); // Buffer 2 size
        assert(expectedSize == 16);
        arrow = readIntoArrowBuffer(buffer, expectedSize);
        assert(arrow.getDouble(0) == 11111111.5);
        assert(arrow.getDouble(8) == 22222222.5);

        expectedSize = buffer.getInt(); // Buffer 3 size (varchar offsets)
        assert(expectedSize == 12);
        offsets = readIntoArrowBuffer(buffer, expectedSize);
        assert(offsets.getInt(0) == 0);
        assert(offsets.getInt(4) == 3);
        assert(offsets.getInt(8) == 6);

        expectedSize = buffer.getInt(); // Buffer 4 size (varchar values)
        assert(expectedSize == 6);
        arrow = readIntoArrowBuffer(buffer, expectedSize);
        assert(getString(arrow, offsets.getInt(0), offsets.getInt(4)).equals("foo"));
        assert(getString(arrow, offsets.getInt(4), offsets.getInt(8)).equals("bar"));

        assert(!buffer.hasRemaining());
    }

    @Test
    public void testFlushOnClose() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        int expectedSize;

        iStream.write(new AugmentedString(1234567890, '\n'));

        iStream.close();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(buffer.getInt() == 1); // # buffers

        expectedSize = buffer.getInt(); // Buffer 1 size
        assert(expectedSize == 4);
        assert(readIntoArrowBuffer(buffer, expectedSize).getInt(0) == 1234567890);

        assert(!buffer.hasRemaining());
    }

    @Test
    public void testCloseWithoutNewline() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        int expectedSize;

        iStream.write(new AugmentedString(1234567890.5));

        iStream.close();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(buffer.getInt() == 1); // # buffers

        expectedSize = buffer.getInt(); // Buffer 1 size
        assert(expectedSize == 8);
        assert(readIntoArrowBuffer(buffer, expectedSize).getDouble(0) == 1234567890.5);

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
