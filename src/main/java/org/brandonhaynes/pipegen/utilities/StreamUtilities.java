package org.brandonhaynes.pipegen.utilities;

import io.netty.buffer.ArrowBuf;
import org.apache.arrow.vector.ValueVector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

public class StreamUtilities {
    private static ByteBuffer integerConversionBuffer = ByteBuffer.allocate(4);

    public static boolean readVectors(InputStream stream, CompositeVector vector, byte[] buffer)
            throws IOException {
        for(ValueVector v: vector.getVectors())
            if(!readVector(stream, v, buffer))
                return false;
        return true;
    }


    private static boolean readVector(InputStream stream, ValueVector vector, byte[] buffer)
            throws IOException {
        Optional<Integer> size = readOptionalInteger(stream);

        if(!size.isPresent())
            return false;

        vector.getMutator().setValueCount(size.get());
        ArrowBuf[] buffers = vector.getBuffers(false);
        vector.getMutator().setValueCount(0);

        for(ArrowBuf arrowBuffer: buffers)
            readBuffer(arrowBuffer, stream, buffer, readInteger(stream));
        return true;
    }

    private static void readBuffer(ArrowBuf arrowBuffer, InputStream stream, byte[] buffer, int size)
            throws IOException {
        arrowBuffer.writeBytes(readBuffer(stream, buffer, size), 0, size);
    }

    private static byte[] readBuffer(InputStream stream, byte[] buffer, int size)
            throws IOException {
        int index = 0, read;

        while(size > 0) {
            if ((read = stream.read(buffer, index, size)) == -1)
                throw new IOException(String.format("Expected %d more bytes, but only read %d", size, read));
            size -= read;
            index += read;
        }

        return buffer;
    }

    public static synchronized byte[] convertInteger(int value) {
        return ((ByteBuffer)integerConversionBuffer.clear()).putInt(value).array();
    }

    private static synchronized int readInteger(InputStream stream) throws IOException {
        readBuffer(stream, ((ByteBuffer)integerConversionBuffer.clear()).array(), 4);
        return integerConversionBuffer.getInt();
    }

    private static synchronized Optional<Integer> readOptionalInteger(InputStream stream) throws IOException {
        try {
            readBuffer(stream, ((ByteBuffer) integerConversionBuffer.clear()).array(), 4);
            return Optional.of(integerConversionBuffer.getInt());
        } catch(IOException e) {
            return Optional.empty();
        }
    }
}
