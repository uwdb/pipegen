package org.brandonhaynes.pipegen.optimization.interprocedural;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ArrayIndexTest extends OptimizationTest  {
    String[] child() {
        int[] ints = {0, 1, 2, 3};
        String[] strings = new String[4];

        for(int i = 0; i < ints.length; i++)
            strings[i] = Integer.toString(ints[i]);
        return strings;
    }

    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        String[] strings = child();

        for(int i = 0; i < strings.length; i++) {
            assert (strings[i] instanceof AugmentedString);
            assert (strings[i].equals(Integer.toString(i)));
            assert (((AugmentedString) strings[i]).getState()[0].equals(i));
        }

        writer.write(strings[0]);
    }
}