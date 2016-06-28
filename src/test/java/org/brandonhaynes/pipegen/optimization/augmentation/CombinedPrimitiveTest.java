package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class CombinedPrimitiveTest extends OptimizationTest {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        String s = Long.toString(999L) + 888 + 7 + 6.5f + 4.3d + "asdf";

        assert(s instanceof AugmentedString);
        assert(s.equals("99988876.54.3asdf"));

        writer.write(s);

        s += "abc" + Long.toString(999L) + 888 + 7 + 6.5f + 4.3d + "xyz" + "\n";

        assert(s instanceof AugmentedString);
        assert(s.equals("99988876.54.3asdfabc99988876.54.3xyz\n"));

        ((AugmentedString)s).printState(System.out);

        writer.write(s);
    }
}
