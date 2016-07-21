package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedStringBuffer;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class StringBufferAppendEntireSubstringTest extends OptimizationTest  {

    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();
        StringBuffer buffer = new StringBuffer();
        String string = "abcdef";
        AugmentedString aString = new AugmentedString(string);

        buffer.append(aString, 0, string.length());

        assert(buffer instanceof AugmentedStringBuffer);
        assert(buffer.toString() instanceof AugmentedString);
        assert(buffer.toString().equals(aString));
        assert(buffer.toString() == aString);

        writer.write(buffer.toString());
    }
}