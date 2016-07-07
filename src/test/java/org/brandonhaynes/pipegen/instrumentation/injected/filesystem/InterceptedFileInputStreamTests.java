package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class InterceptedFileInputStreamTests {
    @Test
    public void testMetadata() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        CompositeVector vector = inStream.getVector();
        assert (vector.getVectors().size() == 3);
        assert (vector.getVectors().get(0).getClass() == IntVector.class);
        assert (vector.getVectors().get(1).getClass() == Float8Vector.class);
        assert (vector.getVectors().get(2).getClass() == VarCharVector.class);
    }

    @Test
    public void testReadByte() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        ByteBuffer buffer = ByteBuffer.allocate(10);
        for(int i = 0; i < "1,1.5,foo\n".length(); i++)
            buffer.put((byte)inStream.read());

        assert(new String(buffer.array()).equals("1,1.5,foo\n"));
        assert(inStream.read() == -1);
    }

    @Test
    public void testMultilineReadByte() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        ByteBuffer buffer = ByteBuffer.allocate(20);
        for(int i = 0; i < 2 * "1,1.5,foo\n".length(); i++)
            buffer.put((byte)inStream.read());

        assert(new String(buffer.array()).equals("1,1.5,foo\n2,2.5,bar\n"));
        assert(inStream.read() == -1);
    }

    @Test
    public void testReadBytes() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        byte[] bytes = new byte[20];
        assert(inStream.read(bytes) == 20);

        assert(new String(bytes).equals("1,1.5,foo\n2,2.5,bar\n"));
        assert(inStream.read() == -1);
    }

    @Test
    public void testReadFewerBytes() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        byte[] bytes = new byte[10];
        assert(inStream.read(bytes) == 10);
        assert(new String(bytes).equals("1,1.5,foo\n"));

        byte[] bytes2 = new byte[9];
        assert(inStream.read(bytes2) == 9);
        assert(new String(bytes2).equals("2,2.5,bar"));

        assert(inStream.read() != -1);
        assert(inStream.read() == -1);
    }

    @Test
    public void testReadMoreBytes() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        byte[] bytes = new byte[30];
        assert(inStream.read(bytes) == 20);
        assert(new String(bytes, 0, 20).equals("1,1.5,foo\n2,2.5,bar\n"));

        assert(inStream.read() == -1);
    }

    @Test
    public void testReadBytesOffset() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        byte[] bytes = new byte[6];
        int read = inStream.read(bytes, 2, 6);
        assert(read == 6);
        assert(new String(bytes).equals("1.5,fo"));

        read = inStream.read(bytes, 3, 6);
        assert(read == 6);
        assert(new String(bytes).equals(",2.5,b"));
    }

    @Test
    public void testReadLine() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        AugmentedString line = inStream.readLine();
        assert(line.getState()[0].equals(1));
        assert(line.getState()[1].equals(1.5));
        assert(line.getState()[2].toString().equals("foo"));

        line = inStream.readLine();
        assert(line.getState()[0].equals(2));
        assert(line.getState()[1].equals(2.5));
        assert(line.getState()[2].toString().equals("bar"));

        assert(inStream.readLine() == null);
    }

    @Test
    public void testMixedReadLine() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        AugmentedString line = inStream.readLine();
        assert(line.getState()[0].equals(1));
        assert(line.getState()[1].equals(1.5));
        assert(line.getState()[2].toString().equals("foo"));

        byte[] bytes = new byte[6];
        int read = inStream.read(bytes, 2, 6);
        assert(read == 6);
        assert(new String(bytes).equals("2.5,ba"));

        assert(inStream.readLine().equals("r\n"));
    }

    @Test
    public void testMixedReadLine2() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        InterceptedFileOutputStream outStream = new InterceptedFileOutputStream(stream);
        outStream.write(new AugmentedString(1, ',', 1.5, ',', "foo", '\n'));
        outStream.write(new AugmentedString(2, ',', 2.5, ',', "bar", '\n'));
        outStream.close();

        InterceptedFileInputStream inStream =
                new InterceptedFileInputStream(new ByteArrayInputStream(stream.toByteArray()));

        byte[] bytes = new byte[8];
        int read = inStream.read(bytes, 0, 8);
        assert(read == 8);
        assert(new String(bytes).equals("1,1.5,fo"));

        AugmentedString line = inStream.readLine();
        assert(line.getState()[0].equals("o\n"));

        line = inStream.readLine();
        assert(line.getState()[0].equals(2));
        assert(line.getState()[1].equals(2.5));
        assert(line.getState()[2].toString().equals("bar"));
    }
}
