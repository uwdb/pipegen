package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import io.netty.buffer.ArrowBuf;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
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
    public void testMetadata() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        iStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        InterceptMetadata metadata = InterceptMetadata.read(buffer);

        assert(metadata.filename == null);
        assert(metadata.vectorClasses[0] == IntVector.class);
        assert(metadata.vectorClasses[1] == Float8Vector.class);
        assert(metadata.vectorClasses[2] == VarCharVector.class);
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
        ArrowBuf offsets;

        iStream.write(new AugmentedString(123456789));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString(987654321.5));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString("foo"));
        iStream.write(new AugmentedString('\n'));

        iStream.flush();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(InterceptMetadata.read(buffer) != null);
        assert(buffer.getInt() == 4); // # buffers

        assertColumn(buffer, 4, new Integer[] {123456789});
        assertColumn(buffer, 8, new Double[] {987654321.5});
        offsets = assertColumn(buffer, 8, new Integer[] {0, 3});
        assertColumn(buffer, offsets, 3, new String[] {"foo"});

        assert(!buffer.hasRemaining());
    }

    @Test
    public void testMultiRowVectorFlush() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);
        ArrowBuf offsets;

        iStream.write(new AugmentedString(123456789));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString(11111111.5));
        iStream.write(new AugmentedString(','));
        iStream.write(new AugmentedString("foo"));
        iStream.write(new AugmentedString('\n'));
        iStream.write(new AugmentedString(234567890, ',', 22222222.5, ',', "bar", "\n"));

        iStream.flush();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(InterceptMetadata.read(buffer) != null);
        assert(buffer.getInt() == 4); // # buffers

        assertColumn(buffer, 8, new Integer[] {123456789, 234567890});
        assertColumn(buffer, 16, new Double[] {11111111.5, 22222222.5});
        offsets = assertColumn(buffer, 12, new Integer[] {0, 3, 6});
        assertColumn(buffer, offsets, 6, new String[] {"foo", "bar"});

        assert(!buffer.hasRemaining());
    }

    @Test
    public void testFlushOnClose() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);

        iStream.write(new AugmentedString(1234567890, '\n'));

        iStream.close();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(InterceptMetadata.read(buffer) != null);
        assert(buffer.getInt() == 1); // # buffers

        assertColumn(buffer, 4, new Integer[] {1234567890});
    }

    @Test
    public void testCloseWithoutNewline() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream iStream = new InterceptedFileOutputStream(stream);

        iStream.write(new AugmentedString(1234567890.5));

        iStream.close();

        ByteBuffer buffer = ByteBuffer.wrap(stream.toByteArray());
        assert(InterceptMetadata.read(buffer) != null);
        assert(buffer.getInt() == 1); // # buffers

        assertColumn(buffer, 8, new Double[] {1234567890.5});

        assert(!buffer.hasRemaining());
    }

    static ArrowBuf assertColumn(ByteBuffer buffer, int expectedSize, Object[] values) {
        return assertColumn(buffer, null, expectedSize, values);
    }

    static ArrowBuf assertColumn(ByteBuffer buffer, ArrowBuf offsets, int expectedSize, Object[] values) {
        assert(buffer.getInt() == expectedSize);

        ArrowBuf arrow = readIntoArrowBuffer(buffer, expectedSize);

        for(int index = 0; index < values.length; index++)
            if(values.getClass().getComponentType() == Integer.class)
                assert (values[index].equals(arrow.getInt(index * Integer.BYTES)));
            else if(values.getClass().getComponentType() == Float.class)
                assert (values[index].equals(arrow.getFloat(index * Float.BYTES)));
            else if(values.getClass().getComponentType() == Double.class)
                assert (values[index].equals(arrow.getDouble(index * Double.BYTES)));
            else if(values.getClass().getComponentType() == String.class)
                assert (values[index].equals(getString(arrow, offsets.getInt(index * Integer.BYTES),
                        offsets.getInt(index * Integer.BYTES + 4))));

        return arrow;
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
