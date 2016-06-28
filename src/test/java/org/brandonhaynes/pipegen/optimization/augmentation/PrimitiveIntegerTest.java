package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class PrimitiveIntegerTest extends OptimizationTest {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        int primitive = 5000;
        String converted = Integer.toString(primitive);
        int anotherPrimitive = 100000;

        converted = converted + anotherPrimitive;

        assert(converted instanceof AugmentedString);
        assert(converted.equals("5000100000"));

        writer.write(converted);
    }
}
