package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class PrimitiveFloatTest extends OptimizationTest {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        float primitive = 5000.5f;
        String converted = Float.toString(primitive);
        float anotherPrimitive = 100.5f;

        converted = converted + anotherPrimitive;

        assert(converted instanceof AugmentedString);
        assert(converted.equals("5000.5100.5"));

        writer.write(converted);
    }
}
