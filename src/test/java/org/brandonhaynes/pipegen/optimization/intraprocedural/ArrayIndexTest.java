package org.brandonhaynes.pipegen.optimization.intraprocedural;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ArrayIndexTest extends OptimizationTest  {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        Integer[] boxed = new Integer[] {1, 2};

        String converted = boxed[0].toString();

        assert(converted instanceof AugmentedString);
        assert(converted.equals("1"));
        assert(((AugmentedString)converted).getState()[0].equals(1));

        int[] unboxed = new int[] {1, 2};

        String converted2 = Integer.toString(unboxed[0]);

        assert(converted2 instanceof AugmentedString);
        assert(converted2.equals("1"));
        assert(((AugmentedString)converted2).getState()[0].equals(1));

        writer.write(converted);
        writer.write(converted2);
    }
}