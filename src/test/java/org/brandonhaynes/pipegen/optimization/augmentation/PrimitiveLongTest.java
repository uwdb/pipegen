package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class PrimitiveLongTest extends OptimizationTest {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        long primitive = 888L;
        String converted = Long.toString(primitive);
        long anotherPrimitive = 99999999999L;

        converted = converted + anotherPrimitive;

        assert(converted instanceof AugmentedString);
        assert(converted.equals("88899999999999"));

        writer.write(converted);
    }
}
