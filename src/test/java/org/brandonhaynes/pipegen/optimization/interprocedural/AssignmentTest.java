package org.brandonhaynes.pipegen.optimization.interprocedural;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class AssignmentTest extends OptimizationTest  {
    private String child() {
        return Integer.toString(5);
    }

    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        String s = child();

        assert(s instanceof AugmentedString);
        assert(s.equals("5"));
        assert(((AugmentedString)s).getState()[0].equals(5));

        writer.write(s);
    }
}