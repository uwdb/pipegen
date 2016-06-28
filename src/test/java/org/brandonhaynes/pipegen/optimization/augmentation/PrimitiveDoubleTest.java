package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class PrimitiveDoubleTest extends OptimizationTest {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        double primitive = 5000.5d;
        String converted = Double.toString(primitive);
        double anotherPrimitive = 999.5d;

        converted = converted + anotherPrimitive;

        assert(converted instanceof AugmentedString);
        assert(converted.equals("5000.5999.5"));

        writer.write(converted);
    }
}
