package org.brandonhaynes.pipegen.utilities;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import java.nio.ByteBuffer;

public class StringUtilities {
    public static AugmentedString intersperse(AugmentedString value, char delimiter, char suffix) {
        Object[] state = new Object[value.getState().length * 2];
        for(int i = 0; i < value.getState().length; i++) {
            state[2 * i] = value.getState()[i];
            state[2 * i + 1] = i + 1 < value.getState().length ? delimiter : suffix;
        }
        return new AugmentedString(state);
    }

    public static String toString(ByteBuffer buffer) {
        byte bytes[] = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }
}
