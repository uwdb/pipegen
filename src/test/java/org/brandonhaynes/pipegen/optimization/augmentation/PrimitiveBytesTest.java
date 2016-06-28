package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class PrimitiveBytesTest extends OptimizationTest {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        byte primitive = 5;
        String converted = Integer.toString(primitive);
        byte anotherPrimitive = 10;

        converted = converted + anotherPrimitive;

        assert(converted instanceof AugmentedString);
        assert(converted.equals("510"));

        writer.write(converted);
    }
}
