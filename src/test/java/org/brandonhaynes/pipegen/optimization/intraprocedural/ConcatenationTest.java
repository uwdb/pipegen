package org.brandonhaynes.pipegen.optimization.intraprocedural;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ConcatenationTest extends OptimizationTest  {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        String left = "5";
        String right = "10";

        String concat = left + right;
        assert(concat instanceof AugmentedString);
        assert(concat.equals("510"));

        writer.write(concat);
    }
}