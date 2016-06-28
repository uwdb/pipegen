package org.brandonhaynes.pipegen.optimization.intraprocedural;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ConvertedTwiceWriterTest extends OptimizationTest  {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        int primitive = 5;
        String converted = Integer.toString(primitive);
        assert(converted instanceof AugmentedString);

        String convertedAgain = converted.toString();
        assert(convertedAgain instanceof AugmentedString);

        writer.write(convertedAgain);
    }
}