package org.brandonhaynes.pipegen.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class StreamUtilities {
    private static ByteBuffer integerConversionBuffer = ByteBuffer.allocate(4);

    public static ByteBuffer readBuffer(InputStream stream, ByteBuffer buffer, int size)
            throws IOException {
        int index = 0, read;

        while(size > 0) {
            if ((read = stream.read(buffer.array(), index, size)) == -1)
                throw new IOException(String.format("Expected %d more bytes, but only read %d", size, read));
            size -= read;
            index += read;
        }

        return buffer;
    }

    public static synchronized byte[] convertInteger(int value) {
        return ((ByteBuffer)integerConversionBuffer.clear()).putInt(value).array();
    }
}
