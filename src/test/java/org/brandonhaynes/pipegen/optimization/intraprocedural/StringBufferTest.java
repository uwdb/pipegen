package org.brandonhaynes.pipegen.optimization.intraprocedural;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedStringBuffer;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class StringBufferTest extends OptimizationTest  {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();
        StringBuffer buffer = new StringBuffer();

        buffer.append("a");
        buffer.append("bc");
        assert(buffer instanceof AugmentedStringBuffer);
        assert(buffer.toString() instanceof AugmentedString);
        assert(buffer.toString().equals("abc"));

        writer.write(buffer.toString());
    }
}